import axios from "axios";
import { useCallback, useEffect, useRef, useState, type CSSProperties, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  deleteCandidat,
  fetchCandidats,
  importCandidatsExcel,
  updateCandidat,
  type CandidatDto,
  type CandidatUpdatePayload,
} from "../api/candidatsApi";
import { fetchConcours, type ConcoursDto } from "../api/concoursApi";
import { useAuth } from "../auth/AuthContext";

function dtoToForm(c: CandidatDto): CandidatUpdatePayload {
  return {
    nom: c.nom,
    prenom: c.prenom,
    cin: c.cin,
    numeroTelephone: c.numeroTelephone,
    ville: c.ville,
    age: c.age,
    email: c.email,
    specialite: c.specialite,
    numeroInscription: c.numeroInscription,
    nomConcours: c.nomConcours,
    concoursId: c.concoursId,
    idCentre: c.idCentre,
    idEtablissement: c.idEtablissement,
    idSalle: c.idSalle,
    numeroPlace: c.numeroPlace,
  };
}

export default function CandidatsPage() {
  const { state, logout } = useAuth();
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [candidats, setCandidats] = useState<CandidatDto[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [importBusy, setImportBusy] = useState(false);
  const [importFeedback, setImportFeedback] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [editing, setEditing] = useState<CandidatDto | null>(null);
  const [editForm, setEditForm] = useState<CandidatUpdatePayload | null>(null);
  const [saving, setSaving] = useState(false);
  const [concoursList, setConcoursList] = useState<ConcoursDto[]>([]);

  const loadList = useCallback(async () => {
    setLoading(true);
    setLoadError(null);
    try {
      const list = await fetchCandidats();
      setCandidats(list);
    } catch (e) {
      setCandidats(null);
      if (axios.isAxiosError(e) && e.response?.status === 404) {
        setLoadError("L’API GET /api/candidats n’est pas disponible (404).");
      } else if (axios.isAxiosError(e) && e.response?.status === 403) {
        setLoadError("Accès refusé (403). Vérifiez le rôle ou le jeton.");
      } else if (axios.isAxiosError(e) && !e.response) {
        setLoadError(
          "Impossible de joindre candidat-service (réseau ou service arrêté). Vérifiez le proxy et le port 8082.",
        );
      } else {
        setLoadError(e instanceof Error ? e.message : "Erreur lors du chargement.");
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      await loadList();
      if (cancelled) return;
    })();
    return () => {
      cancelled = true;
    };
  }, [loadList]);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const list = await fetchConcours();
        if (!cancelled) setConcoursList(list);
      } catch {
        if (!cancelled) setConcoursList([]);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  if (state.status !== "authenticated") {
    return null;
  }

  const { user } = state;
  const readOnly = user.role === "ADMINISTRATEUR";

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  function openEdit(c: CandidatDto) {
    setActionError(null);
    setEditing(c);
    setEditForm(dtoToForm(c));
  }

  function closeEdit() {
    setEditing(null);
    setEditForm(null);
    setSaving(false);
  }

  async function handleSaveEdit(e: FormEvent) {
    e.preventDefault();
    if (!editing || !editForm) return;
    setSaving(true);
    setActionError(null);
    try {
      const updated = await updateCandidat(editing.id, editForm);
      setCandidats((prev) =>
        prev ? prev.map((x) => (x.id === updated.id ? updated : x)) : [updated],
      );
      closeEdit();
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 409) {
        setActionError(
          (err.response?.data as { message?: string })?.message ??
            "Conflit : CIN ou numéro d’inscription déjà utilisé.",
        );
      } else if (axios.isAxiosError(err) && err.response?.data) {
        const d = err.response.data as { message?: string; detail?: string };
        setActionError(d.message ?? d.detail ?? "Erreur lors de l’enregistrement.");
      } else {
        setActionError(err instanceof Error ? err.message : "Erreur lors de l’enregistrement.");
      }
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(c: CandidatDto) {
    const ok = window.confirm(
      `Supprimer le candidat ${c.prenom} ${c.nom} (CIN ${c.cin}) ? Cette action est définitive.`,
    );
    if (!ok) return;
    setActionError(null);
    try {
      await deleteCandidat(c.id);
      setCandidats((prev) => (prev ? prev.filter((x) => x.id !== c.id) : prev));
    } catch (err) {
      setActionError(err instanceof Error ? err.message : "Erreur lors de la suppression.");
    }
  }

  function triggerImportClick() {
    setImportFeedback(null);
    fileInputRef.current?.click();
  }

  async function handleFileSelected(ev: React.ChangeEvent<HTMLInputElement>) {
    const file = ev.target.files?.[0];
    ev.target.value = "";
    if (!file) return;
    if (!file.name.toLowerCase().endsWith(".xlsx")) {
      setImportFeedback("Veuillez choisir un fichier Excel au format .xlsx.");
      return;
    }
    setImportBusy(true);
    setImportFeedback(null);
    setActionError(null);
    try {
      const res = await importCandidatsExcel(file);
      const parts = [
        `${res.inserted} créé(s)`,
        `${res.updated} mis à jour`,
        res.skipped > 0 ? `${res.skipped} ignoré(s)` : null,
      ].filter(Boolean);
      setImportFeedback(parts.join(" · ") + ".");
      if (res.errors.length > 0) {
        const preview = res.errors
          .slice(0, 5)
          .map((x) => `Ligne ${x.rowNumber}: ${x.message}`)
          .join(" — ");
        setImportFeedback((prev) => `${prev} Détails : ${preview}${res.errors.length > 5 ? " …" : ""}`);
      }
      await loadList();
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 403) {
        setImportFeedback("Import réservé au gestionnaire (403).");
      } else if (axios.isAxiosError(err) && err.response?.status === 400) {
        const d = err.response.data as { message?: string; detail?: string };
        setImportFeedback(d.message ?? d.detail ?? "Fichier refusé (400).");
      } else {
        setImportFeedback(err instanceof Error ? err.message : "Échec de l’import.");
      }
    } finally {
      setImportBusy(false);
    }
  }

  return (
    <div style={page}>
      <header style={header}>
        <div style={headerInner}>
          <div style={titleRow}>
            <h1 style={title}>Candidats</h1>
            <nav style={nav}>
              <Link style={navLinkActive} to="/candidats">
                Candidats
              </Link>
              <Link style={navLink} to="/concours">
                Concours
              </Link>
              <Link style={navLink} to="/lieux">
                Lieux
              </Link>
            </nav>
          </div>
          <div style={headerActions}>
            <span style={meta}>
              {user.username} · {user.role}
              {readOnly ? " (lecture seule)" : ""}
            </span>
            <button type="button" style={btnGhost} onClick={handleLogout}>
              Déconnexion
            </button>
          </div>
        </div>
      </header>
      <main style={main}>
        <p style={lead}>
          Liste des candidats admis. Le gestionnaire peut importer un fichier Excel (.xlsx), puis consulter, modifier ou
          supprimer chaque ligne.
        </p>

        <section style={section}>
          <div style={toolbar}>
            <h2 style={h2Inline}>Liste des candidats</h2>
            {!readOnly ? (
              <div style={toolbarRight}>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                  style={{ display: "none" }}
                  onChange={handleFileSelected}
                />
                <button
                  type="button"
                  style={btnPrimary}
                  onClick={triggerImportClick}
                  disabled={importBusy || loading}
                >
                  {importBusy ? "Import…" : "Importer fichier Excel"}
                </button>
              </div>
            ) : null}
          </div>
          {importFeedback ? (
            <p role="status" style={infoBanner}>
              {importFeedback}
            </p>
          ) : null}
          {actionError ? (
            <p role="alert" style={alert}>
              {actionError}
            </p>
          ) : null}
          {loading ? <p style={muted}>Chargement…</p> : null}
          {!loading && loadError ? (
            <p role="alert" style={alert}>
              {loadError}
            </p>
          ) : null}
          {!loading && !loadError && candidats && candidats.length === 0 ? (
            <p style={muted}>Aucun candidat. Importez un fichier Excel ou ajoutez des données côté serveur.</p>
          ) : null}
          {!loading && !loadError && candidats && candidats.length > 0 ? (
            <div style={tableWrap}>
              <table style={table}>
                <thead>
                  <tr>
                    <th style={th}>Nom</th>
                    <th style={th}>Prénom</th>
                    <th style={th}>CIN</th>
                    <th style={th}>Tél.</th>
                    <th style={th}>Ville</th>
                    <th style={th}>Âge</th>
                    <th style={th}>Email</th>
                    <th style={th}>Spécialité</th>
                    <th style={th}>N° inscr.</th>
                    <th style={th}>Concours</th>
                    {!readOnly ? <th style={thActions}>Actions</th> : null}
                  </tr>
                </thead>
                <tbody>
                  {candidats.map((c) => (
                    <tr key={c.id}>
                      <td style={td}>{c.nom}</td>
                      <td style={td}>{c.prenom}</td>
                      <td style={td}>{c.cin}</td>
                      <td style={td}>{c.numeroTelephone}</td>
                      <td style={td}>{c.ville}</td>
                      <td style={td}>{c.age}</td>
                      <td style={tdEmail}>{c.email}</td>
                      <td style={td}>{c.specialite}</td>
                      <td style={td}>{c.numeroInscription}</td>
                      <td style={td}>{c.nomConcours}</td>
                      {!readOnly ? (
                        <td style={tdActions}>
                          <button type="button" style={btnLink} onClick={() => openEdit(c)}>
                            Modifier
                          </button>
                          <button type="button" style={btnDanger} onClick={() => void handleDelete(c)}>
                            Supprimer
                          </button>
                        </td>
                      ) : null}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}
          <p style={hint}>
            Format Excel : en-tête avec colonnes Nom, Prénom, CIN, Téléphone, Ville, Âge, Email, Spécialité, Numéro
            d&apos;inscription, Nom du concours — ou dix colonnes dans cet ordre sans ligne d&apos;en-tête.
          </p>
        </section>
      </main>

      {editing && editForm ? (
        <div style={modalBackdrop} onClick={closeEdit} role="presentation">
          <div
            style={modal}
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="edit-candidat-title"
          >
            <h2 id="edit-candidat-title" style={modalTitle}>
              Modifier le candidat
            </h2>
            <form onSubmit={(e) => void handleSaveEdit(e)}>
              <div style={formGrid}>
                <label style={label}>
                  Nom
                  <input
                    style={input}
                    value={editForm.nom}
                    onChange={(e) => setEditForm({ ...editForm, nom: e.target.value })}
                    required
                  />
                </label>
                <label style={label}>
                  Prénom
                  <input
                    style={input}
                    value={editForm.prenom}
                    onChange={(e) => setEditForm({ ...editForm, prenom: e.target.value })}
                    required
                  />
                </label>
                <label style={label}>
                  CIN
                  <input
                    style={input}
                    value={editForm.cin}
                    onChange={(e) => setEditForm({ ...editForm, cin: e.target.value })}
                    required
                  />
                </label>
                <label style={label}>
                  Téléphone
                  <input
                    style={input}
                    value={editForm.numeroTelephone}
                    onChange={(e) => setEditForm({ ...editForm, numeroTelephone: e.target.value })}
                    required
                  />
                </label>
                <label style={label}>
                  Ville
                  <input
                    style={input}
                    value={editForm.ville}
                    onChange={(e) => setEditForm({ ...editForm, ville: e.target.value })}
                    required
                  />
                </label>
                <label style={label}>
                  Âge
                  <input
                    style={input}
                    type="number"
                    min={10}
                    max={120}
                    value={editForm.age}
                    onChange={(e) => setEditForm({ ...editForm, age: Number(e.target.value) })}
                    required
                  />
                </label>
                <label style={{ ...label, gridColumn: "1 / -1" }}>
                  Email
                  <input
                    style={input}
                    type="email"
                    value={editForm.email}
                    onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
                    required
                  />
                </label>
                <label style={{ ...label, gridColumn: "1 / -1" }}>
                  Spécialité
                  <input
                    style={input}
                    value={editForm.specialite}
                    onChange={(e) => setEditForm({ ...editForm, specialite: e.target.value })}
                    required
                  />
                </label>
                <label style={label}>
                  N° inscription
                  <input
                    style={input}
                    value={editForm.numeroInscription}
                    onChange={(e) => setEditForm({ ...editForm, numeroInscription: e.target.value })}
                    required
                  />
                </label>
                <label style={{ ...label, gridColumn: "1 / -1" }}>
                  Concours
                  <select
                    style={input}
                    value={editForm.concoursId ?? ""}
                    onChange={(e) => {
                      const id = e.target.value === "" ? null : Number(e.target.value);
                      const co = concoursList.find((x) => x.id === id);
                      setEditForm({
                        ...editForm,
                        concoursId: id,
                        nomConcours: co?.nomConcours ?? editForm.nomConcours,
                      });
                    }}
                    required
                  >
                    <option value="" disabled>
                      Choisir un concours…
                    </option>
                    {concoursList.map((co) => (
                      <option key={co.id} value={co.id}>
                        {co.nomConcours}
                        {co.numeroConcours ? ` (${co.numeroConcours})` : ""}
                      </option>
                    ))}
                  </select>
                </label>
                <label style={label}>
                  Centre ID (optionnel)
                  <input
                    style={input}
                    type="number"
                    value={editForm.idCentre ?? ""}
                    onChange={(e) =>
                      setEditForm({
                        ...editForm,
                        idCentre: e.target.value === "" ? null : Number(e.target.value),
                      })
                    }
                  />
                </label>
                <label style={label}>
                  Établissement ID (optionnel)
                  <input
                    style={input}
                    type="number"
                    value={editForm.idEtablissement ?? ""}
                    onChange={(e) =>
                      setEditForm({
                        ...editForm,
                        idEtablissement: e.target.value === "" ? null : Number(e.target.value),
                      })
                    }
                  />
                </label>
                <label style={label}>
                  Salle ID (optionnel)
                  <input
                    style={input}
                    type="number"
                    value={editForm.idSalle ?? ""}
                    onChange={(e) =>
                      setEditForm({
                        ...editForm,
                        idSalle: e.target.value === "" ? null : Number(e.target.value),
                      })
                    }
                  />
                </label>
                <label style={label}>
                  N° place (optionnel)
                  <input
                    style={input}
                    type="number"
                    min={1}
                    value={editForm.numeroPlace ?? ""}
                    onChange={(e) =>
                      setEditForm({
                        ...editForm,
                        numeroPlace: e.target.value === "" ? null : Number(e.target.value),
                      })
                    }
                  />
                </label>
              </div>
              {actionError ? (
                <p role="alert" style={{ ...alert, marginTop: "1rem" }}>
                  {actionError}
                </p>
              ) : null}
              <div style={modalActions}>
                <button type="button" style={btnGhost} onClick={closeEdit} disabled={saving}>
                  Annuler
                </button>
                <button type="submit" style={btnPrimary} disabled={saving}>
                  {saving ? "Enregistrement…" : "Enregistrer"}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </div>
  );
}

const page: CSSProperties = {
  minHeight: "100vh",
  background: "#f8fafc",
  color: "#0f172a",
};

const titleRow: CSSProperties = {
  display: "flex",
  alignItems: "center",
  gap: "1.25rem",
  flexWrap: "wrap",
};

const nav: CSSProperties = { display: "flex", gap: "0.5rem", alignItems: "center" };

const navLink: CSSProperties = {
  fontSize: "0.9rem",
  fontWeight: 600,
  color: "#2563eb",
  textDecoration: "none",
};

const navLinkActive: CSSProperties = {
  ...navLink,
  color: "#0f172a",
  textDecoration: "underline",
};

const header: CSSProperties = {
  background: "#fff",
  borderBottom: "1px solid #e2e8f0",
  boxShadow: "0 1px 2px rgba(15,23,42,0.04)",
};

const headerInner: CSSProperties = {
  maxWidth: "1200px",
  margin: "0 auto",
  padding: "1rem 1.5rem",
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "1rem",
  flexWrap: "wrap",
};

const title: CSSProperties = {
  margin: 0,
  fontSize: "1.35rem",
  fontWeight: 700,
};

const headerActions: CSSProperties = {
  display: "flex",
  alignItems: "center",
  gap: "1rem",
};

const meta: CSSProperties = {
  fontSize: "0.875rem",
  color: "#64748b",
};

const btnGhost: CSSProperties = {
  padding: "0.45rem 0.85rem",
  borderRadius: "8px",
  border: "1px solid #cbd5e1",
  background: "#fff",
  cursor: "pointer",
  fontWeight: 600,
  fontSize: "0.875rem",
};

const btnPrimary: CSSProperties = {
  padding: "0.5rem 1rem",
  borderRadius: "8px",
  border: "none",
  background: "#2563eb",
  color: "#fff",
  cursor: "pointer",
  fontWeight: 600,
  fontSize: "0.875rem",
};

const main: CSSProperties = {
  maxWidth: "1200px",
  margin: "0 auto",
  padding: "2rem 1.5rem",
};

const lead: CSSProperties = {
  marginTop: 0,
  fontSize: "1rem",
  lineHeight: 1.6,
  color: "#334155",
};

const section: CSSProperties = {
  marginTop: "1.5rem",
  padding: "1.25rem",
  background: "#fff",
  borderRadius: "12px",
  border: "1px solid #e2e8f0",
};

const toolbar: CSSProperties = {
  display: "flex",
  alignItems: "center",
  justifyContent: "space-between",
  gap: "1rem",
  flexWrap: "wrap",
  marginBottom: "1rem",
};

const toolbarRight: CSSProperties = {
  display: "flex",
  gap: "0.5rem",
};

const h2Inline: CSSProperties = {
  margin: 0,
  fontSize: "1.1rem",
};

const muted: CSSProperties = {
  color: "#64748b",
  margin: 0,
};

const alert: CSSProperties = {
  color: "#b45309",
  background: "#fffbeb",
  border: "1px solid #fcd34d",
  padding: "0.75rem 1rem",
  borderRadius: "8px",
  margin: "0 0 1rem",
};

const infoBanner: CSSProperties = {
  color: "#1e40af",
  background: "#eff6ff",
  border: "1px solid #93c5fd",
  padding: "0.75rem 1rem",
  borderRadius: "8px",
  margin: "0 0 1rem",
  fontSize: "0.9rem",
};

const tableWrap: CSSProperties = {
  overflowX: "auto",
  marginTop: "0.5rem",
};

const table: CSSProperties = {
  width: "100%",
  borderCollapse: "collapse",
  fontSize: "0.8125rem",
};

const th: CSSProperties = {
  textAlign: "left",
  padding: "0.5rem 0.4rem",
  borderBottom: "2px solid #e2e8f0",
  color: "#475569",
  fontWeight: 600,
  whiteSpace: "nowrap",
};

const thActions: CSSProperties = {
  ...th,
  textAlign: "right",
  minWidth: "140px",
};

const td: CSSProperties = {
  padding: "0.45rem 0.4rem",
  borderBottom: "1px solid #f1f5f9",
  verticalAlign: "top",
};

const tdEmail: CSSProperties = {
  ...td,
  maxWidth: "160px",
  wordBreak: "break-word",
};

const tdActions: CSSProperties = {
  ...td,
  textAlign: "right",
  whiteSpace: "nowrap",
};

const btnLink: CSSProperties = {
  border: "none",
  background: "none",
  color: "#2563eb",
  cursor: "pointer",
  fontWeight: 600,
  fontSize: "0.8125rem",
  marginRight: "0.5rem",
};

const btnDanger: CSSProperties = {
  border: "none",
  background: "none",
  color: "#dc2626",
  cursor: "pointer",
  fontWeight: 600,
  fontSize: "0.8125rem",
};

const hint: CSSProperties = {
  margin: "1rem 0 0",
  fontSize: "0.8rem",
  color: "#64748b",
  lineHeight: 1.5,
};

const modalBackdrop: CSSProperties = {
  position: "fixed",
  inset: 0,
  background: "rgba(15,23,42,0.45)",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  padding: "1rem",
  zIndex: 50,
};

const modal: CSSProperties = {
  background: "#fff",
  borderRadius: "12px",
  padding: "1.5rem",
  maxWidth: "560px",
  width: "100%",
  maxHeight: "90vh",
  overflowY: "auto",
  boxShadow: "0 20px 40px rgba(15,23,42,0.15)",
};

const modalTitle: CSSProperties = {
  marginTop: 0,
  marginBottom: "1rem",
  fontSize: "1.15rem",
};

const formGrid: CSSProperties = {
  display: "grid",
  gridTemplateColumns: "1fr 1fr",
  gap: "0.75rem 1rem",
};

const label: CSSProperties = {
  display: "flex",
  flexDirection: "column",
  gap: "0.25rem",
  fontSize: "0.8rem",
  fontWeight: 600,
  color: "#475569",
};

const input: CSSProperties = {
  padding: "0.45rem 0.55rem",
  borderRadius: "6px",
  border: "1px solid #cbd5e1",
  fontSize: "0.875rem",
};

const modalActions: CSSProperties = {
  display: "flex",
  justifyContent: "flex-end",
  gap: "0.5rem",
  marginTop: "1.25rem",
};

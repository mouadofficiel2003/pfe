import axios from "axios";
import { useCallback, useEffect, useState, type CSSProperties, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  createConcours,
  deleteConcours,
  fetchConcours,
  updateConcours,
  type CentreAffectationWrite,
  type ConcoursDto,
  type ConcoursWritePayload,
} from "../api/concoursApi";
import { useAuth } from "../auth/AuthContext";

type CentreFormRow = { nomCentre: string; centreId: string };

function isoToDatetimeLocal(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "";
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function rowsToPayload(
  nomConcours: string,
  numeroConcours: string,
  dateLocal: string,
  rows: CentreFormRow[],
): ConcoursWritePayload {
  const centres: CentreAffectationWrite[] = rows
    .filter((r) => r.nomCentre.trim())
    .map((r) => ({
      nomCentre: r.nomCentre.trim(),
      centreId: r.centreId.trim() ? Number(r.centreId.trim()) : null,
    }));
  return {
    nomConcours: nomConcours.trim(),
    numeroConcours: numeroConcours.trim() || null,
    dateHeureExamen: new Date(dateLocal).toISOString(),
    centres,
  };
}

export default function ConcoursPage() {
  const { state, logout } = useAuth();
  const navigate = useNavigate();
  const [list, setList] = useState<ConcoursDto[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [nomConcours, setNomConcours] = useState("");
  const [numeroConcours, setNumeroConcours] = useState("");
  const [dateLocal, setDateLocal] = useState("");
  const [centreRows, setCentreRows] = useState<CentreFormRow[]>([{ nomCentre: "", centreId: "" }]);
  const [saving, setSaving] = useState(false);

  const loadList = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchConcours();
      setList(data);
    } catch (e) {
      setList(null);
      if (axios.isAxiosError(e) && e.code === "ECONNABORTED") {
        setError("Délai dépassé : redémarrez concours-service (8083) et lieux-service (8084), puis rechargez la page.");
      } else if (axios.isAxiosError(e) && !e.response) {
        setError("Impossible de joindre concours-service (port 8083 ou proxy Vite).");
      } else {
        setError(e instanceof Error ? e.message : "Erreur de chargement.");
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadList();
  }, [loadList]);

  if (state.status !== "authenticated") {
    return null;
  }

  const { user } = state;
  const readOnly = user.role === "ADMINISTRATEUR";

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  function openCreate() {
    setActionError(null);
    setEditingId(null);
    setNomConcours("");
    setNumeroConcours("");
    setDateLocal("");
    setCentreRows([{ nomCentre: "", centreId: "" }]);
    setFormOpen(true);
  }

  function openEdit(c: ConcoursDto) {
    setActionError(null);
    setEditingId(c.id);
    setNomConcours(c.nomConcours);
    setNumeroConcours(c.numeroConcours ?? "");
    setDateLocal(isoToDatetimeLocal(c.dateHeureExamen));
    setCentreRows(
      c.centres.length
        ? c.centres.map((x) => ({ nomCentre: x.nomCentre, centreId: x.centreId != null ? String(x.centreId) : "" }))
        : [{ nomCentre: "", centreId: "" }],
    );
    setFormOpen(true);
  }

  function closeForm() {
    setFormOpen(false);
    setEditingId(null);
    setSaving(false);
  }

  function addCentreRow() {
    setCentreRows((r) => [...r, { nomCentre: "", centreId: "" }]);
  }

  function removeCentreRow(index: number) {
    setCentreRows((r) => (r.length <= 1 ? r : r.filter((_, i) => i !== index)));
  }

  function setCentreRow(index: number, field: keyof CentreFormRow, value: string) {
    setCentreRows((rows) => rows.map((row, i) => (i === index ? { ...row, [field]: value } : row)));
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setSaving(true);
    setActionError(null);
    if (!dateLocal.trim()) {
      setActionError("Indiquez la date et l’heure d’examen.");
      setSaving(false);
      return;
    }
    const payload = rowsToPayload(nomConcours, numeroConcours, dateLocal, centreRows);
    if (!payload.centres.length) {
      setActionError("Ajoutez au moins un centre (nom du centre).");
      setSaving(false);
      return;
    }
    try {
      if (editingId == null) {
        await createConcours(payload);
      } else {
        await updateConcours(editingId, payload);
      }
      closeForm();
      await loadList();
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 403) {
        setActionError("Modification réservée au gestionnaire.");
      } else if (axios.isAxiosError(err) && err.response?.status === 409) {
        setActionError("Numéro de concours déjà utilisé.");
      } else {
        setActionError(err instanceof Error ? err.message : "Échec de l’enregistrement.");
      }
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(c: ConcoursDto) {
    if (!window.confirm(`Supprimer le concours « ${c.nomConcours} » ?`)) return;
    setActionError(null);
    try {
      await deleteConcours(c.id);
      await loadList();
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 403) {
        setActionError("Suppression réservée au gestionnaire.");
      } else {
        setActionError(err instanceof Error ? err.message : "Échec de la suppression.");
      }
    }
  }

  return (
    <div style={page}>
      <header style={header}>
        <div style={headerInner}>
          <div style={titleRow}>
            <h1 style={title}>Concours</h1>
            <nav style={nav}>
              <Link style={navLink} to="/candidats">
                Candidats
              </Link>
              <Link style={navLinkActive} to="/concours">
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
          Création des concours : nom, date et heure d’examen, et centres où le concours se déroule (ex. « Centre Rabat » —
          aligné avec la ville du candidat pour la répartition).
        </p>

        <section style={section}>
          <div style={toolbar}>
            <h2 style={h2Inline}>Liste des concours</h2>
            {!readOnly ? (
              <button type="button" style={btnPrimary} onClick={openCreate} disabled={loading}>
                Nouveau concours
              </button>
            ) : null}
          </div>
          {actionError ? (
            <p role="alert" style={alert}>
              {actionError}
            </p>
          ) : null}
          {loading ? <p style={muted}>Chargement…</p> : null}
          {!loading && error ? (
            <p role="alert" style={alert}>
              {error}
            </p>
          ) : null}
          {!loading && !error && list && list.length === 0 ? <p style={muted}>Aucun concours enregistré.</p> : null}
          {!loading && !error && list && list.length > 0 ? (
            <div style={tableWrap}>
              <table style={table}>
                <thead>
                  <tr>
                    <th style={th}>Nom</th>
                    <th style={th}>N° concours</th>
                    <th style={th}>Date / heure</th>
                    <th style={th}>Centres</th>
                    {!readOnly ? <th style={thActions}>Actions</th> : null}
                  </tr>
                </thead>
                <tbody>
                  {list.map((c) => (
                    <tr key={c.id}>
                      <td style={td}>{c.nomConcours}</td>
                      <td style={td}>{c.numeroConcours ?? "—"}</td>
                      <td style={td}>{new Date(c.dateHeureExamen).toLocaleString()}</td>
                      <td style={td}>
                        {c.centres.map((x) => x.nomCentre).join(", ") || "—"}
                      </td>
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
        </section>

        {formOpen ? (
          <div style={modalBackdrop} role="presentation" onMouseDown={closeForm}>
            <div
              style={modal}
              role="dialog"
              aria-modal="true"
              onMouseDown={(ev) => ev.stopPropagation()}
            >
              <h2 style={modalTitle}>{editingId == null ? "Nouveau concours" : "Modifier le concours"}</h2>
              <form onSubmit={(e) => void handleSubmit(e)}>
                <div style={formGrid}>
                  <label style={{ ...label, gridColumn: "1 / -1" }}>
                    Nom du concours
                    <input
                      style={input}
                      value={nomConcours}
                      onChange={(e) => setNomConcours(e.target.value)}
                      required
                      maxLength={200}
                    />
                  </label>
                  <label style={label}>
                    N° concours (optionnel)
                    <input
                      style={input}
                      value={numeroConcours}
                      onChange={(e) => setNumeroConcours(e.target.value)}
                      maxLength={80}
                    />
                  </label>
                  <label style={label}>
                    Date et heure d’examen
                    <input
                      style={input}
                      type="datetime-local"
                      value={dateLocal}
                      onChange={(e) => setDateLocal(e.target.value)}
                      required
                    />
                  </label>
                </div>
                <p style={hint}>Centres — au moins une ligne. Le nom doit correspondre à la logique de répartition (ville / centre).</p>
                {centreRows.map((row, index) => (
                  <div key={index} style={centreRow}>
                    <label style={{ ...label, flex: 2 }}>
                      Nom du centre
                      <input
                        style={input}
                        value={row.nomCentre}
                        onChange={(e) => setCentreRow(index, "nomCentre", e.target.value)}
                        placeholder="ex. Centre Rabat"
                        maxLength={200}
                      />
                    </label>
                    <label style={{ ...label, flex: 1 }}>
                      ID centre (optionnel)
                      <input
                        style={input}
                        value={row.centreId}
                        onChange={(e) => setCentreRow(index, "centreId", e.target.value)}
                        placeholder="lieux"
                        inputMode="numeric"
                      />
                    </label>
                    {!readOnly && centreRows.length > 1 ? (
                      <button type="button" style={btnGhost} onClick={() => removeCentreRow(index)}>
                        Retirer
                      </button>
                    ) : null}
                  </div>
                ))}
                {!readOnly ? (
                  <button type="button" style={btnGhost} onClick={addCentreRow}>
                    + Ajouter un centre
                  </button>
                ) : null}
                <div style={modalActions}>
                  <button type="button" style={btnGhost} onClick={closeForm}>
                    Annuler
                  </button>
                  {!readOnly ? (
                    <button type="submit" style={btnPrimary} disabled={saving}>
                      {saving ? "Enregistrement…" : "Enregistrer"}
                    </button>
                  ) : null}
                </div>
              </form>
            </div>
          </div>
        ) : null}
      </main>
    </div>
  );
}

const page: CSSProperties = { minHeight: "100vh", background: "#f8fafc" };

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

const titleRow: CSSProperties = {
  display: "flex",
  alignItems: "center",
  gap: "1.25rem",
  flexWrap: "wrap",
};

const title: CSSProperties = {
  margin: 0,
  fontSize: "1.35rem",
  fontWeight: 700,
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
  maxWidth: "640px",
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

const hint: CSSProperties = {
  margin: "1rem 0 0.5rem",
  fontSize: "0.8rem",
  color: "#64748b",
};

const centreRow: CSSProperties = {
  display: "flex",
  gap: "0.75rem",
  alignItems: "flex-end",
  marginBottom: "0.5rem",
  flexWrap: "wrap",
};

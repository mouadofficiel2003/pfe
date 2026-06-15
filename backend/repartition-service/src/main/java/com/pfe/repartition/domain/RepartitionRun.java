package com.pfe.repartition.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Une exécution de la répartition automatique (déclenchement unique du gestionnaire). */
@Entity
@Table(name = "repartition_run")
public class RepartitionRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "declenche_par", length = 120)
    private String declenchePar;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 40)
    private RepartitionStatut statut;

    @Column(name = "total_candidats", nullable = false)
    private int totalCandidats;

    @Column(name = "total_affectes", nullable = false)
    private int totalAffectes;

    @Column(name = "total_alertes", nullable = false)
    private int totalAlertes;

    @Column(name = "demarre_le", nullable = false)
    private Instant demarreLe;

    @Column(name = "termine_le")
    private Instant termineLe;

    @Column(name = "message", length = 500)
    private String message;

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<RepartitionAffectation> affectations = new ArrayList<>();

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<RepartitionAlerte> alertes = new ArrayList<>();

    public void ajouterAffectation(RepartitionAffectation a) {
        a.setRun(this);
        affectations.add(a);
    }

    public void ajouterAlerte(RepartitionAlerte a) {
        a.setRun(this);
        alertes.add(a);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeclenchePar() {
        return declenchePar;
    }

    public void setDeclenchePar(String declenchePar) {
        this.declenchePar = declenchePar;
    }

    public RepartitionStatut getStatut() {
        return statut;
    }

    public void setStatut(RepartitionStatut statut) {
        this.statut = statut;
    }

    public int getTotalCandidats() {
        return totalCandidats;
    }

    public void setTotalCandidats(int totalCandidats) {
        this.totalCandidats = totalCandidats;
    }

    public int getTotalAffectes() {
        return totalAffectes;
    }

    public void setTotalAffectes(int totalAffectes) {
        this.totalAffectes = totalAffectes;
    }

    public int getTotalAlertes() {
        return totalAlertes;
    }

    public void setTotalAlertes(int totalAlertes) {
        this.totalAlertes = totalAlertes;
    }

    public Instant getDemarreLe() {
        return demarreLe;
    }

    public void setDemarreLe(Instant demarreLe) {
        this.demarreLe = demarreLe;
    }

    public Instant getTermineLe() {
        return termineLe;
    }

    public void setTermineLe(Instant termineLe) {
        this.termineLe = termineLe;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<RepartitionAffectation> getAffectations() {
        return affectations;
    }

    public List<RepartitionAlerte> getAlertes() {
        return alertes;
    }
}

package com.pfe.repartition.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Alerte produite quand un candidat n'a pas pu être affecté (capacité dépassée, aucun centre, etc.). */
@Entity
@Table(name = "repartition_alerte")
public class RepartitionAlerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private RepartitionRun run;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private AlerteType type;

    @Column(name = "numero_inscription", length = 80)
    private String numeroInscription;

    @Column(name = "candidat_nom", length = 250)
    private String candidatNom;

    @Column(name = "ville", length = 120)
    private String ville;

    @Column(name = "numero_concours", length = 80)
    private String numeroConcours;

    @Column(name = "nom_concours", length = 200)
    private String nomConcours;

    @Column(name = "centre_id")
    private Long centreId;

    @Column(name = "nom_centre", length = 200)
    private String nomCentre;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RepartitionRun getRun() {
        return run;
    }

    public void setRun(RepartitionRun run) {
        this.run = run;
    }

    public AlerteType getType() {
        return type;
    }

    public void setType(AlerteType type) {
        this.type = type;
    }

    public String getNumeroInscription() {
        return numeroInscription;
    }

    public void setNumeroInscription(String numeroInscription) {
        this.numeroInscription = numeroInscription;
    }

    public String getCandidatNom() {
        return candidatNom;
    }

    public void setCandidatNom(String candidatNom) {
        this.candidatNom = candidatNom;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getNumeroConcours() {
        return numeroConcours;
    }

    public void setNumeroConcours(String numeroConcours) {
        this.numeroConcours = numeroConcours;
    }

    public String getNomConcours() {
        return nomConcours;
    }

    public void setNomConcours(String nomConcours) {
        this.nomConcours = nomConcours;
    }

    public Long getCentreId() {
        return centreId;
    }

    public void setCentreId(Long centreId) {
        this.centreId = centreId;
    }

    public String getNomCentre() {
        return nomCentre;
    }

    public void setNomCentre(String nomCentre) {
        this.nomCentre = nomCentre;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

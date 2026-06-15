package com.pfe.repartition.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Résultat d'affectation d'un candidat (candidat → centre, établissement, salle, place) pour un run. */
@Entity
@Table(name = "repartition_affectation")
public class RepartitionAffectation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private RepartitionRun run;

    @Column(name = "numero_inscription", nullable = false, length = 80)
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

    @Column(name = "etablissement_id")
    private Long etablissementId;

    @Column(name = "nom_etablissement", length = 200)
    private String nomEtablissement;

    @Column(name = "salle_id")
    private Long salleId;

    @Column(name = "nom_salle", length = 200)
    private String nomSalle;

    @Column(name = "numero_place")
    private Integer numeroPlace;

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

    public Long getEtablissementId() {
        return etablissementId;
    }

    public void setEtablissementId(Long etablissementId) {
        this.etablissementId = etablissementId;
    }

    public String getNomEtablissement() {
        return nomEtablissement;
    }

    public void setNomEtablissement(String nomEtablissement) {
        this.nomEtablissement = nomEtablissement;
    }

    public Long getSalleId() {
        return salleId;
    }

    public void setSalleId(Long salleId) {
        this.salleId = salleId;
    }

    public String getNomSalle() {
        return nomSalle;
    }

    public void setNomSalle(String nomSalle) {
        this.nomSalle = nomSalle;
    }

    public Integer getNumeroPlace() {
        return numeroPlace;
    }

    public void setNumeroPlace(Integer numeroPlace) {
        this.numeroPlace = numeroPlace;
    }
}

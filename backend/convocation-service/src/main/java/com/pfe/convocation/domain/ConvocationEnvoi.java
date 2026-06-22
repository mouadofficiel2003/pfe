package com.pfe.convocation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Trace de l'envoi (réussi ou en échec) d'une convocation par e-mail à un candidat.
 * Les références (numero_inscription, numero_concours) sont logiques (autres services).
 */
@Entity
@Table(name = "convocation_envoi")
public class ConvocationEnvoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_inscription", nullable = false, length = 80)
    private String numeroInscription;

    @Column(name = "candidat_nom", length = 250)
    private String candidatNom;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "numero_concours", length = 80)
    private String numeroConcours;

    @Column(name = "nom_concours", length = 200)
    private String nomConcours;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private EnvoiStatut statut;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "declenche_par", length = 120)
    private String declenchePar;

    @Column(name = "envoye_le", nullable = false)
    private Instant envoyeLe;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public EnvoiStatut getStatut() {
        return statut;
    }

    public void setStatut(EnvoiStatut statut) {
        this.statut = statut;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDeclenchePar() {
        return declenchePar;
    }

    public void setDeclenchePar(String declenchePar) {
        this.declenchePar = declenchePar;
    }

    public Instant getEnvoyeLe() {
        return envoyeLe;
    }

    public void setEnvoyeLe(Instant envoyeLe) {
        this.envoyeLe = envoyeLe;
    }
}

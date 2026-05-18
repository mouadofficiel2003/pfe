package com.pfe.candidats.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Persistance : colonnes PostgreSQL en snake_case via {@link Column#name()}.
 * Si cette entité est un jour sérialisée en JSON, Jackson utilisera les noms Java en camelCase (pas besoin de {@code @JsonProperty} tant qu'ils restent alignés avec le front).
 */
@Entity
@Table(name = "candidat")
public class Candidat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nom;

    @Column(nullable = false, length = 120)
    private String prenom;

    @Column(nullable = false, length = 32, unique = true)
    private String cin;

    @Column(name = "numero_telephone", nullable = false, length = 32)
    private String numeroTelephone;

    @Column(nullable = false, length = 120)
    private String ville;

    @Column(nullable = false)
    private short age;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 200)
    private String specialite;

    @Column(name = "numero_inscription", nullable = false, length = 80)
    private String numeroInscription;

    @Column(name = "nom_concours", nullable = false, length = 200)
    private String nomConcours;

    @Column(name = "concours_id")
    private Long concoursId;

    @Column(name = "id_centre")
    private Long idCentre;

    @Column(name = "id_etablissement")
    private Long idEtablissement;

    @Column(name = "id_salle")
    private Long idSalle;

    @Column(name = "numero_place")
    private Integer numeroPlace;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "modifie_le", nullable = false)
    private Instant modifieLe;

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getCin() {
        return cin;
    }

    public String getNumeroTelephone() {
        return numeroTelephone;
    }

    public String getVille() {
        return ville;
    }

    public short getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public String getSpecialite() {
        return specialite;
    }

    public String getNumeroInscription() {
        return numeroInscription;
    }

    public String getNomConcours() {
        return nomConcours;
    }

    public Long getConcoursId() {
        return concoursId;
    }

    public Long getIdCentre() {
        return idCentre;
    }

    public Long getIdEtablissement() {
        return idEtablissement;
    }

    public Long getIdSalle() {
        return idSalle;
    }

    public Integer getNumeroPlace() {
        return numeroPlace;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public Instant getModifieLe() {
        return modifieLe;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public void setNumeroTelephone(String numeroTelephone) {
        this.numeroTelephone = numeroTelephone;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public void setAge(short age) {
        this.age = age;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public void setNumeroInscription(String numeroInscription) {
        this.numeroInscription = numeroInscription;
    }

    public void setNomConcours(String nomConcours) {
        this.nomConcours = nomConcours;
    }

    public void setConcoursId(Long concoursId) {
        this.concoursId = concoursId;
    }

    public void setIdCentre(Long idCentre) {
        this.idCentre = idCentre;
    }

    public void setIdEtablissement(Long idEtablissement) {
        this.idEtablissement = idEtablissement;
    }

    public void setIdSalle(Long idSalle) {
        this.idSalle = idSalle;
    }

    public void setNumeroPlace(Integer numeroPlace) {
        this.numeroPlace = numeroPlace;
    }

    public void setCreeLe(Instant creeLe) {
        this.creeLe = creeLe;
    }

    public void setModifieLe(Instant modifieLe) {
        this.modifieLe = modifieLe;
    }
}

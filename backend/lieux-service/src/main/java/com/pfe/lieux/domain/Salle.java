package com.pfe.lieux.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "salle")
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_salle")
    private Long idSalle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etablissement_id", referencedColumnName = "id_etablissement", nullable = false)
    private Etablissement etablissement;

    @Column(name = "nom_salle", nullable = false, length = 200)
    private String nomSalle;

    @Column(name = "nombre_places", nullable = false)
    private int nombrePlaces;

    /** Référence logique vers concours.numero_concours (service concours) ; validée à l'écriture via API. */
    @Column(name = "numero_concours", length = 80)
    private String numeroConcours;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "modifie_le", nullable = false)
    private Instant modifieLe;

    public Long getIdSalle() {
        return idSalle;
    }

    public void setIdSalle(Long idSalle) {
        this.idSalle = idSalle;
    }

    public Etablissement getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(Etablissement etablissement) {
        this.etablissement = etablissement;
    }

    public String getNomSalle() {
        return nomSalle;
    }

    public void setNomSalle(String nomSalle) {
        this.nomSalle = nomSalle;
    }

    public int getNombrePlaces() {
        return nombrePlaces;
    }

    public void setNombrePlaces(int nombrePlaces) {
        this.nombrePlaces = nombrePlaces;
    }

    public String getNumeroConcours() {
        return numeroConcours;
    }

    public void setNumeroConcours(String numeroConcours) {
        this.numeroConcours = numeroConcours;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(Instant creeLe) {
        this.creeLe = creeLe;
    }

    public Instant getModifieLe() {
        return modifieLe;
    }

    public void setModifieLe(Instant modifieLe) {
        this.modifieLe = modifieLe;
    }
}

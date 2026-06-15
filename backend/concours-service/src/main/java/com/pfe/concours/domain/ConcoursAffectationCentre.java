package com.pfe.concours.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "concours_affectation_centre")
public class ConcoursAffectationCentre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "numero_concours", referencedColumnName = "numero_concours", nullable = false)
    private Concours concours;

    @Column(name = "id_centre", nullable = false)
    private Long idCentre;

    @Column(name = "nom_centre", nullable = false, length = 200)
    private String nomCentre;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Concours getConcours() {
        return concours;
    }

    public void setConcours(Concours concours) {
        this.concours = concours;
    }

    public Long getIdCentre() {
        return idCentre;
    }

    public void setIdCentre(Long idCentre) {
        this.idCentre = idCentre;
    }

    public String getNomCentre() {
        return nomCentre;
    }

    public void setNomCentre(String nomCentre) {
        this.nomCentre = nomCentre;
    }
}

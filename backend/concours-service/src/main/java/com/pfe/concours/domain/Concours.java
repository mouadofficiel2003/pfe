package com.pfe.concours.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "concours")
public class Concours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_concours", nullable = false, length = 200)
    private String nomConcours;

    @Column(name = "numero_concours", length = 80, unique = true)
    private String numeroConcours;

    @Column(name = "date_heure_examen", nullable = false)
    private Instant dateHeureExamen;

    @OneToMany(mappedBy = "concours", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConcoursAffectationCentre> affectationsCentres = new ArrayList<>();

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "modifie_le", nullable = false)
    private Instant modifieLe;

    public void lierAffectations() {
        for (ConcoursAffectationCentre a : affectationsCentres) {
            a.setConcours(this);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomConcours() {
        return nomConcours;
    }

    public void setNomConcours(String nomConcours) {
        this.nomConcours = nomConcours;
    }

    public String getNumeroConcours() {
        return numeroConcours;
    }

    public void setNumeroConcours(String numeroConcours) {
        this.numeroConcours = numeroConcours;
    }

    public Instant getDateHeureExamen() {
        return dateHeureExamen;
    }

    public void setDateHeureExamen(Instant dateHeureExamen) {
        this.dateHeureExamen = dateHeureExamen;
    }

    public List<ConcoursAffectationCentre> getAffectationsCentres() {
        return affectationsCentres;
    }

    public void setAffectationsCentres(List<ConcoursAffectationCentre> affectationsCentres) {
        this.affectationsCentres = affectationsCentres;
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

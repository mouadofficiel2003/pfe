package com.pfe.lieux.domain;

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
@Table(name = "centre")
public class Centre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_centre")
    private Long idCentre;

    @Column(name = "nom_centre", nullable = false, length = 200, unique = true)
    private String nomCentre;

    @OneToMany(mappedBy = "centre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Etablissement> etablissements = new ArrayList<>();

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "modifie_le", nullable = false)
    private Instant modifieLe;

    public void lierEtablissements() {
        for (Etablissement e : etablissements) {
            e.setCentre(this);
        }
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

    public List<Etablissement> getEtablissements() {
        return etablissements;
    }

    public void setEtablissements(List<Etablissement> etablissements) {
        this.etablissements = etablissements;
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

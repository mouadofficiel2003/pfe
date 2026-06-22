package com.pfe.convocation.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Génère la convocation d'un candidat au format PDF (OpenPDF). */
@Component
public class ConvocationPdfGenerator {

    private static final Color BLEU = new Color(0x1F, 0x3A, 0x5F);
    private static final Color GRIS = new Color(0xF2, 0xF4, 0xF7);

    private final DateTimeFormatter dateFormatter;
    private final DateTimeFormatter heureFormatter;

    public ConvocationPdfGenerator(@Value("${convocation.zone:Africa/Casablanca}") String zone) {
        ZoneId zoneId = ZoneId.of(zone);
        this.dateFormatter =
                DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH).withZone(zoneId);
        this.heureFormatter =
                DateTimeFormatter.ofPattern("HH'h'mm", Locale.FRENCH).withZone(zoneId);
    }

    public byte[] genererPdf(ConvocationData data) {
        Document document = new Document(PageSize.A4, 56, 56, 64, 56);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(entete());
            document.add(titre());
            document.add(sousTitre(valeurOuTiret(data.nomConcours())));
            document.add(espace(18));
            document.add(phraseIntro(data));
            document.add(espace(14));
            document.add(tableauInfos(data));
            document.add(espace(22));
            document.add(consignes());

            document.close();
        } catch (DocumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Échec de génération du PDF de convocation : " + e.getMessage());
        }
        return out.toByteArray();
    }

    private Paragraph entete() {
        Paragraph p = new Paragraph(
                "Royaume du Maroc — Organisation des concours",
                FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, Color.GRAY));
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }

    private Paragraph titre() {
        Paragraph p = new Paragraph(
                "CONVOCATION À L'EXAMEN",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Font.BOLD, BLEU));
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingBefore(14);
        return p;
    }

    private Paragraph sousTitre(String nomConcours) {
        Paragraph p = new Paragraph(
                nomConcours, FontFactory.getFont(FontFactory.HELVETICA, 13, Font.NORMAL, Color.DARK_GRAY));
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingBefore(4);
        return p;
    }

    private Paragraph phraseIntro(ConvocationData data) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(
                "Madame, Monsieur ", FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK)));
        p.add(new Phrase(
                valeurOuTiret(data.nomComplet()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Font.BOLD, Color.BLACK)));
        p.add(new Phrase(
                ", vous êtes officiellement convoqué(e) pour passer l'épreuve aux date, lieu et place"
                        + " indiqués ci-dessous.",
                FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK)));
        p.setAlignment(Element.ALIGN_JUSTIFIED);
        return p;
    }

    private PdfPTable tableauInfos(ConvocationData data) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] {38, 62});

        ligne(table, "Nom et prénom", valeurOuTiret(data.nomComplet()));
        ligne(table, "Numéro d'inscription", valeurOuTiret(data.numeroInscription()));
        ligne(table, "Numéro du concours", valeurOuTiret(data.numeroConcours()));
        ligne(table, "Nom du concours", valeurOuTiret(data.nomConcours()));
        ligne(table, "Date de l'examen", formatDate(data));
        ligne(table, "Heure de l'examen", formatHeure(data));
        ligne(table, "Centre d'examen", valeurOuTiret(data.nomCentre()));
        ligne(table, "Établissement", valeurOuTiret(data.nomEtablissement()));
        ligne(table, "Salle", valeurOuTiret(data.nomSalle()));
        ligne(table, "Place", data.numeroPlace() != null ? "N° " + data.numeroPlace() : "—");
        return table;
    }

    private void ligne(PdfPTable table, String libelle, String valeur) {
        PdfPCell cLibelle = new PdfPCell(new Phrase(
                libelle, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f, Font.BOLD, BLEU)));
        cLibelle.setBackgroundColor(GRIS);
        cLibelle.setPadding(8);
        cLibelle.setBorderColor(Color.LIGHT_GRAY);

        PdfPCell cValeur = new PdfPCell(new Phrase(
                valeur, FontFactory.getFont(FontFactory.HELVETICA, 10.5f, Font.NORMAL, Color.BLACK)));
        cValeur.setPadding(8);
        cValeur.setBorderColor(Color.LIGHT_GRAY);

        table.addCell(cLibelle);
        table.addCell(cValeur);
    }

    private Paragraph consignes() {
        Paragraph p = new Paragraph(
                "Veuillez vous présenter au centre d'examen au moins 30 minutes avant le début de"
                        + " l'épreuve, muni(e) de cette convocation et d'une pièce d'identité officielle en"
                        + " cours de validité. Aucun candidat ne sera admis après le début de l'épreuve.",
                FontFactory.getFont(FontFactory.HELVETICA, 9.5f, Font.ITALIC, Color.DARK_GRAY));
        p.setAlignment(Element.ALIGN_JUSTIFIED);
        p.setSpacingBefore(10);
        return p;
    }

    private static Paragraph espace(float hauteur) {
        Paragraph p = new Paragraph(" ");
        p.setLeading(hauteur);
        return p;
    }

    private String formatDate(ConvocationData data) {
        return data.dateHeureExamen() != null ? capitaliser(dateFormatter.format(data.dateHeureExamen())) : "—";
    }

    private String formatHeure(ConvocationData data) {
        return data.dateHeureExamen() != null ? heureFormatter.format(data.dateHeureExamen()) : "—";
    }

    private static String capitaliser(String texte) {
        if (texte == null || texte.isEmpty()) {
            return texte;
        }
        return Character.toUpperCase(texte.charAt(0)) + texte.substring(1);
    }

    private static String valeurOuTiret(String valeur) {
        return valeur == null || valeur.isBlank() ? "—" : valeur;
    }
}

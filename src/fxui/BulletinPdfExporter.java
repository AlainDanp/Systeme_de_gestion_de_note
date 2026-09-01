package fxui;

import gestion_Bulletin.model.Bulletin;
import gestion_Bulletin.model.NoteDetail;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Génère le bulletin scolaire en PDF selon un gabarit fixe (en-tête, infos, tableau des notes, pied de page). */
public final class BulletinPdfExporter {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final float MARGE = 55;
    private static final float COL_MATIERE = MARGE;
    private static final float COL_NOTE = MARGE + 260;
    private static final float COL_ENSEIGNANT = MARGE + 340;

    private BulletinPdfExporter() {}

    public static void export(File fichier, Bulletin bulletin, List<NoteDetail> notes) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            float largeur = page.getMediaBox().getWidth();

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - MARGE;
                y = enTete(cs, largeur, y);
                y = infosGenerales(cs, y, bulletin);
                y = tableauNotes(cs, y, notes);
                piedDePage(cs, bulletin, y);
            }

            document.save(fichier);
        }
    }

    private static float enTete(PDPageContentStream cs, float largeur, float y) throws IOException {
        texte(cs, PDType1Font.HELVETICA_BOLD, 20, MARGE, y, "Bulletin scolaire");
        y -= 12;

        cs.setStrokingColor(0.55f, 0.55f, 0.55f);
        cs.moveTo(MARGE, y);
        cs.lineTo(largeur - MARGE, y);
        cs.stroke();
        return y - 28;
    }

    private static float infosGenerales(PDPageContentStream cs, float y, Bulletin bulletin) throws IOException {
        y = ligneInfo(cs, y, "Étudiant :", bulletin.getEtudiantNomComplet());
        y = ligneInfo(cs, y, "Classe :", bulletin.getClasseNiveau() == null ? "-" : bulletin.getClasseNiveau());
        y = ligneInfo(cs, y, "Période :", bulletin.getPeriode());
        y = ligneInfo(cs, y, "Moyenne générale :", formatMoyenne(bulletin.getMoyenne()));
        y = ligneInfo(cs, y, "Moyenne de la classe :", formatMoyenne(bulletin.getMoyennDelaClasse()));
        return y - 15;
    }

    private static float tableauNotes(PDPageContentStream cs, float y, List<NoteDetail> notes) throws IOException {
        texte(cs, PDType1Font.HELVETICA_BOLD, 11, COL_MATIERE, y, "Matière");
        texte(cs, PDType1Font.HELVETICA_BOLD, 11, COL_NOTE, y, "Note /20");
        texte(cs, PDType1Font.HELVETICA_BOLD, 11, COL_ENSEIGNANT, y, "Enseignant");
        y -= 6;

        cs.setStrokingColor(0.55f, 0.55f, 0.55f);
        cs.moveTo(MARGE, y);
        cs.lineTo(COL_ENSEIGNANT + 170, y);
        cs.stroke();
        y -= 18;

        for (NoteDetail note : notes) {
            texte(cs, PDType1Font.HELVETICA, 11, COL_MATIERE, y, note.getMatiere());
            texte(cs, PDType1Font.HELVETICA, 11, COL_NOTE, y, String.format("%.2f", note.getValeur()));
            texte(cs, PDType1Font.HELVETICA, 11, COL_ENSEIGNANT, y, note.getEnseignantNomComplet());
            y -= 18;
        }

        if (notes.isEmpty()) {
            texte(cs, PDType1Font.HELVETICA_OBLIQUE, 11, COL_MATIERE, y, "Aucune note enregistrée pour cette période.");
            y -= 18;
        }

        return y;
    }

    private static void piedDePage(PDPageContentStream cs, Bulletin bulletin, float y) throws IOException {
        String genereLe = "Document généré le " + DTF.format(OffsetDateTime.now());
        String creeLe = bulletin.getCreatedAt() == null ? "" : " — bulletin créé le " + DTF.format(bulletin.getCreatedAt());
        texte(cs, PDType1Font.HELVETICA_OBLIQUE, 9, MARGE, Math.min(y - 10, 60), genereLe + creeLe);
    }

    private static float ligneInfo(PDPageContentStream cs, float y, String label, String valeur) throws IOException {
        texte(cs, PDType1Font.HELVETICA_BOLD, 11, MARGE, y, label);
        texte(cs, PDType1Font.HELVETICA, 11, MARGE + 160, y, valeur == null ? "-" : valeur);
        return y - 18;
    }

    private static void texte(PDPageContentStream cs, PDType1Font font, float taille, float x, float y, String contenu)
            throws IOException {
        cs.beginText();
        cs.setFont(font, taille);
        cs.newLineAtOffset(x, y);
        cs.showText(contenu == null ? "" : contenu);
        cs.endText();
    }

    private static String formatMoyenne(Double valeur) {
        return valeur == null ? "-" : String.format("%.2f / 20", valeur);
    }
}

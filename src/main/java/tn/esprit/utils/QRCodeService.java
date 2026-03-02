package tn.esprit.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class QRCodeService {

    /**
     * Generates a QR Code Image for JavaFX based on text content.
     * 
     * @param text   The content to encode into the QR code
     * @param width  The width of the QR code image
     * @param height The height of the QR code image
     * @return JavaFX Image object
     */
    public static Image generateQRCodeImage(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            byte[] pngData = pngOutputStream.toByteArray();
            return new Image(new ByteArrayInputStream(pngData));

        } catch (Exception e) {
            System.err.println("Error generating QR Code: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generates a QR Code Image and returns it as a raw byte array (PNG format).
     * Useful for embedding into tools like PDFs (iText) that don't accept JavaFX
     * Image types.
     * 
     * @param text   The content to encode into the QR code
     * @param width  The width of the QR code image
     * @param height The height of the QR code image
     * @return Raw PNG byte array of the QR code
     */
    public static byte[] getQRCodeImageBytes(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            return pngOutputStream.toByteArray();

        } catch (Exception e) {
            System.err.println("Error generating QR Code bytes: " + e.getMessage());
            return null;
        }
    }
}

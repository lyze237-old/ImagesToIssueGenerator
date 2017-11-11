package sh.owl;


import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {
    private static List<Pair<String, Color>> defColors;
    private static String text = "0";
    private static Properties codes = new Properties();

    // program file.png character outFile.sh
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("No arguments!");
            System.exit(1);
        }

        if (args.length > 1)
            text = args[1];

        try {
            defColors = getDefColors();
            File file = new File("Resources/Codes.properties");
            codes.load(new FileInputStream(file));
            BufferedImage img = ImageIO.read(new File(args[0]));
            String outString = imageToString(img);
            String fileName;
            if (args.length == 3) {
                fileName = args[2];
            } else
                fileName = "output.sh";
            try (PrintWriter pw = new PrintWriter(fileName)) {
                pw.print(outString);
                System.out.println("Generated output to: " + fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String imageToString(BufferedImage bImage) {
        StringBuilder builder = new StringBuilder();
        builder.append("#!/bin/bash").append(System.lineSeparator()).append(System.lineSeparator());
        builder.append("echo -e \"\\e[0m\" > issue").append(System.lineSeparator()).append(System.lineSeparator());
        for (int heightIndex = 0; heightIndex < bImage.getHeight(); heightIndex++) {
            builder.append("echo -e \"");
            for (int widthIndex = 0; widthIndex < bImage.getWidth(); widthIndex++) {
                Color color = new Color(bImage.getRGB(widthIndex, heightIndex));
                Pair<String, Color> closestColor = null;
                for (Pair<String, Color> defColor : defColors) {
                    if (color.equals(defColor.getValue()))
                        closestColor = defColor;
                }
                if (closestColor == null)
                    closestColor = calculateClosestColor(color);

                builder.append(colorToText(closestColor));
            }
            builder.append("\" >> issue").append(System.lineSeparator());
        }

        builder.append("echo -e \"\\e[0m\" >> issue").append(System.lineSeparator()).append(System.lineSeparator());
        return builder.toString();
    }

    private static String colorToText(Pair<String, Color> color) {
        int code = Integer.parseInt(codes.getProperty(color.getKey().toLowerCase()));
        if (color.getKey().equals("BLACK"))
            return  "\\e[" + code + "m ";
        return "\\e[" + code + "m" + text;
    }

    private static Pair<String, Color> calculateClosestColor(Color color) {
        Pair<String, Color> closest = null;
        int distance = Integer.MAX_VALUE;

        for (Pair<String, Color> defColor : defColors) {
            int thisDistance = Math.abs(color.getRed() - defColor.getValue().getRed())
                    + Math.abs(color.getBlue() - defColor.getValue().getBlue())
                    + Math.abs(color.getGreen() - defColor.getValue().getGreen());
            if (distance > thisDistance) {
                closest = defColor;
                distance = thisDistance;
            }
        }
        System.out.println("Calculated closest color " + closest + " of color " + color);
        return closest;
    }

    private static List<Pair<String, Color>> getDefColors() throws IOException {
        List<Pair<String, Color>> defColors = new ArrayList<>();

        Properties colorProps = new Properties();
        colorProps.load(new FileInputStream("Resources/Colors.properties"));
        colorProps.forEach((o, o2) -> {
            String numbers = o2.toString();
            numbers = numbers.replace("(", "");
            numbers = numbers.replace(")", "");
            String[] numbersArray = numbers.split(",");
            defColors.add(new Pair<>(o.toString(), new Color(Integer.parseInt(numbersArray[0].trim()), Integer.parseInt(numbersArray[1].trim()), Integer.parseInt(numbersArray[2].trim()))));
        });
        return defColors;
    }

}

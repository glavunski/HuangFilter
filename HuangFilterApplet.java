import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class HuangFilterApplet extends JApplet implements ActionListener {

    public final int offsetX = 20;
    public final int offsetY = 40;
    public final int canvasW = 250;
    public final int canvasH = 250;
    public final int canvasSpace = 25;

    public BufferedImage currentImage;
    public JButton imgButton;
    public JButton filterButton;
    public TextField kernelRadiusField;

    public JPanel buttonPane;

    public boolean openedNewImage = false;
    public boolean filterClicked = false;

    public static void main(String[] args) {

        // create and set up the applet
        HuangFilterApplet applet = new HuangFilterApplet();
        applet.setPreferredSize(new Dimension(570, 300));
        applet.init();

        // create a frame to host the applet, which is just another type of Swing Component
        JFrame mainFrame = new JFrame();
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // add the applet to the frame and show it
        mainFrame.getContentPane().add(applet);
        mainFrame.pack();
        mainFrame.setVisible(true);

        // start the applet
        applet.start();
    }

    public void init() {
        buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout());

        imgButton = new JButton("Open Image");
        filterButton = new JButton("Filter");
        imgButton.addActionListener(this);
        filterButton.addActionListener(this);

        kernelRadiusField = new TextField();
        JLabel windowSizeLabel = new JLabel("Kernel radius");

        buttonPane.add(imgButton);
        buttonPane.add(filterButton);
        buttonPane.add(windowSizeLabel);
        buttonPane.add(kernelRadiusField);

        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, offsetX, 10, 10));
        buttonPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        this.add(buttonPane, BorderLayout.WEST);
    }


    public void paint(Graphics g) {
        super.paint(g);

        g.setColor(new Color(0,0,0));
        g.drawRect(offsetX,offsetY,canvasW,canvasH);
        g.drawRect(offsetX + canvasW + canvasSpace,offsetY,canvasW,canvasH);

        if(openedNewImage){
            if(currentImage != null){
                modifyOpenedImage(currentImage);
                g.drawImage(currentImage, offsetX, offsetY, currentImage.getWidth(null), currentImage.getHeight(null), null);
            }
        }

        if(filterClicked){
            if(currentImage != null && !kernelRadiusField.getText().isEmpty()){
                drawFilteredImage(g,currentImage, offsetX + canvasW + canvasSpace,offsetY);
            }
        }

    }

    public void modifyOpenedImage(BufferedImage imageBefore){
        int newWidth = imageBefore.getWidth(null);
        int newHeight = imageBefore.getHeight(null);

        float ratio;

        if(newWidth > canvasW){
            ratio = newWidth / (float)canvasW;
            newWidth = Math.round(newWidth / ratio);
            newHeight = Math.round(newHeight / ratio);
        }

        if(newHeight > canvasH){
            ratio = newHeight / (float)canvasH;
            newHeight = Math.round(newHeight / ratio);
            newWidth = Math.round(newWidth / ratio);
        }

        currentImage = resizeImage(imageBefore,BufferedImage.TYPE_INT_RGB, newWidth,newHeight);
    }


    public void drawFilteredImage(Graphics g,BufferedImage img,int posX,int posY){
        int kernelRadius = Integer.parseInt(kernelRadiusField.getText());
        if(kernelRadius >= 1){
            int imgWidth = img.getWidth(null);
            int imgHeight = img.getHeight(null);
            g.drawImage(img, posX, posY, imgWidth, imgHeight, null);

            //initialize histogram
            Histogram histogram = new Histogram();

            for(int y = 0; y <= kernelRadius; y++){
                for(int x = 0; x <= kernelRadius; x++){
                    Color c = new Color(img.getRGB(x,y));
                    int pixelValueR = c.getRed();
                    int pixelValueG = c.getGreen();
                    int pixelValueB = c.getBlue();

                    histogram.incrementLevelFrequency(pixelValueR,pixelValueG,pixelValueB);
                }
            }

            int[] startMedian = histogram.getMedian();
            //draw first pixel on new picture
            g.setColor(new Color(startMedian[0],startMedian[1],startMedian[2]));
            g.drawLine(posX,posY,posX,posY);

            int lastY1 = -1,lastY2 = -1,lastX = -1;

            for(int cY = 0; cY < imgHeight; cY++){
                for(int cX = 0; cX < imgWidth; cX++){
                    //we start from second pixel
                    if((cY == 0 && cX > 0) || (cY > 0)){

                        //delete last column
                        if(lastX >= 0){
                            for(int startY = lastY1; startY <= lastY2; startY++){
                                Color c = new Color(img.getRGB(lastX,startY));

                                int pixelValueR = c.getRed();
                                int pixelValueG = c.getGreen();
                                int pixelValueB = c.getBlue();

                                histogram.decrementLevelFrequency(pixelValueR,pixelValueG,pixelValueB);

                            }
                        }

                        //set new last column coordinates
                        lastX = cX - kernelRadius;
                        lastY1 = (cY - kernelRadius  >= 0) ? cY - kernelRadius : 0;
                        lastY2 = (cY + kernelRadius < imgHeight) ? cY + kernelRadius : imgHeight - 1;
                        if(lastX < 0 && cY > 0){
                            lastX = imgWidth + lastX;
                            lastY1 = (cY - kernelRadius - 1 >= 0) ? cY - kernelRadius - 1 : 0;
                            lastY2 = (cY + kernelRadius - 1  < imgHeight) ? cY + kernelRadius - 1 : imgHeight - 1;
                        }

                        //add new column
                        int newColumnX = (cX + kernelRadius);
                        int newColumnY1 = (cY - kernelRadius >= 0) ? cY - kernelRadius : 0;
                        int newColumnY2 = (cY + kernelRadius < imgHeight) ? cY + kernelRadius : imgHeight - 1;

                        if(newColumnX >= imgWidth && cY < imgHeight){
                            newColumnX = newColumnX - imgWidth;
                            newColumnY1 = (cY - kernelRadius + 1  >= 0) ? cY - kernelRadius + 1 : 0;
                            newColumnY2 = (cY + kernelRadius + 1  < imgHeight) ? cY + kernelRadius + 1 : imgHeight - 1;
                        }

                        for(int startY = newColumnY1; startY <= newColumnY2; startY++){
                            Color c = new Color(img.getRGB(newColumnX,startY));

                            int pixelValueR = c.getRed();
                            int pixelValueG = c.getGreen();
                            int pixelValueB = c.getBlue();

                            histogram.incrementLevelFrequency(pixelValueR,pixelValueG,pixelValueB);

                        }


                        //draw new pixel on new picture
                        int[] currentMedian = histogram.getMedian();

                        g.setColor(new Color(currentMedian[0],currentMedian[1],currentMedian[2]));
                        g.drawLine(posX + cX,posY + cY,posX + cX,posY + cY);
                    }
                }
            }
        }
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int type,int img_width, int img_height) {
        BufferedImage resizedImage = new BufferedImage(img_width, img_height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, img_width, img_height, null);
        g.dispose();

        return resizedImage;
    }

    public void actionPerformed(ActionEvent e){
        if (e.getSource() == imgButton) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png"));
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                try{
                    File file = fileChooser.getSelectedFile();
                    currentImage = ImageIO.read(file);
                    openedNewImage = true;
                }catch (IOException c){
                    c.printStackTrace();
                }
            }
        }else if(e.getSource() == filterButton){
            filterClicked = true;
        }
        validate();
        repaint();
    }

}

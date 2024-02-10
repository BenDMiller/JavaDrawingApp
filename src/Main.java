import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.*;

class SimplePaintPanel extends JPanel {
   private final Set<Point> blackPixels = new HashSet<Point>();
   private final int brushSize;

   private int mouseButtonDown = 0;

   public SimplePaintPanel() {
      this(3, new HashSet<Point>());
   }

   public SimplePaintPanel(Set<Point> blackPixels) {
      this(3, blackPixels);
   }

   public SimplePaintPanel(int brushSize, Set<Point> blackPixels) {
      this.setPreferredSize(new Dimension(800, 500));
      this.brushSize = brushSize;
      this.blackPixels.addAll(blackPixels);
      final SimplePaintPanel self = this;

      MouseAdapter mouseAdapter = new MouseAdapter() {
         @Override
         public void mouseDragged(MouseEvent ev) {
            if (mouseButtonDown == 1)
               addPixels(getPixelsAround(ev.getPoint()));
            else if (mouseButtonDown == 3)
               removePixels(getPixelsAround(ev.getPoint()));
         }

         @Override
         public void mousePressed(MouseEvent ev) {
            self.mouseButtonDown = ev.getButton();
         }
      };
      this.addMouseMotionListener(mouseAdapter);
      this.addMouseListener(mouseAdapter);

   }

   public void paint(Graphics g) {
      int w = this.getWidth();
      int h = this.getHeight();
      g.setColor(Color.white);
      g.fillRect(0, 0, w, h);
      g.setColor(Color.blue);
      for (Point point : blackPixels)
         g.fillRect(point.x, point.y, 1, 1);

   }

   public void clear() {
      this.blackPixels.clear();
      this.invalidate();
      this.repaint();
   }

   public void addPixels(Collection<? extends Point> blackPixels) {
      this.blackPixels.addAll(blackPixels);
      this.invalidate();
      this.repaint();
   }

   public void removePixels(Collection<? extends Point> blackPixels) {
      this.blackPixels.removeAll(blackPixels);
      this.invalidate();
      this.repaint();
   }

   public boolean isPixel(Point blackPixel) {
      return this.blackPixels.contains(blackPixel);
   }

   private Collection<? extends Point> getPixelsAround(Point point) {
      Set<Point> points = new HashSet<>();
      for (int x = point.x - brushSize; x < point.x + brushSize; x++)
         for (int y = point.y - brushSize; y < point.y + brushSize; y++)
            points.add(new Point(x, y));
      return points;
   }
}

public class Main extends JFrame implements ActionListener {
   private final String ACTION_NEW = "New Image";
   private final String ACTION_LOAD = "Load Image";
   private final String ACTION_SAVE = "Save Image";

   private final SimplePaintPanel paintPanel = new SimplePaintPanel();

   public Main() {
      super();
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setTitle("Drawing App");

      initMenu();
      this.getContentPane().add(paintPanel);

      pack();
      setVisible(true);
   }

   private void initMenu() {
      JMenuBar menuBar = new JMenuBar();
      JMenu menu = new JMenu("File");
      JMenuItem mnuNew = new JMenuItem(ACTION_NEW);
      JMenuItem mnuLoad = new JMenuItem(ACTION_LOAD);
      JMenuItem mnuSave = new JMenuItem(ACTION_SAVE);
      mnuNew.setActionCommand(ACTION_NEW);
      mnuLoad.setActionCommand(ACTION_LOAD);
      mnuSave.setActionCommand(ACTION_SAVE);
      mnuNew.addActionListener(this);
      mnuLoad.addActionListener(this);
      mnuSave.addActionListener(this);
      menu.add(mnuNew);
      menu.add(mnuLoad);
      menu.add(mnuSave);
      menuBar.add(menu);
      this.setJMenuBar(menuBar);
   }

   @Override
   public void actionPerformed(ActionEvent ev) {
      switch (ev.getActionCommand()) {
      case ACTION_NEW:
         paintPanel.clear();
         break;
      case ACTION_LOAD:
         doLoadImage();
         break;
      case ACTION_SAVE:
         doSaveImage();
         break;
      }
   }

   private void doSaveImage() {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int result = fileChooser.showSaveDialog(this);
      if (result != JFileChooser.APPROVE_OPTION)
         return;
      File saveFile = fileChooser.getSelectedFile();
      if (!saveFile.getAbsolutePath().toLowerCase().endsWith(".png"))
         saveFile = new File(saveFile.getAbsolutePath() + ".png");
      BufferedImage image = new BufferedImage(paintPanel.getSize().width, paintPanel.getSize().height,
            BufferedImage.TYPE_INT_RGB);
      for (int x = 0; x < image.getWidth(); x++) {
         for (int y = 0; y < image.getHeight(); y++) {
            image.setRGB(x, y, Color.white.getRGB());
            if (paintPanel.isPixel(new Point(x, y))) {
               image.setRGB(x, y, Color.black.getRGB());
            }
         }
      }
      try {
         ImageIO.write(image, "png", saveFile);
      } catch (IOException e) {
         return;
      }
   }

   private void doLoadImage() {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int result = fileChooser.showOpenDialog(this);
      if (result != JFileChooser.APPROVE_OPTION)
         return;
      BufferedImage image;
      File openFile = fileChooser.getSelectedFile();
      try (FileInputStream fis = new FileInputStream(openFile)) {
         image = ImageIO.read(fis);
      } catch (IOException e) {
         return;
      }
      if (image == null)
         return;
      paintPanel.clear();
      Set<Point> blackPixels = new HashSet<Point>();
      for (int x = 0; x < image.getWidth(); x++) {
         for (int y = 0; y < image.getHeight(); y++) {
            Color c = new Color(image.getRGB(x, y));
            if ((c.getBlue() < 128 || c.getRed() < 128 || c.getGreen() < 128) && c.getAlpha() == 255) {
               blackPixels.add(new Point(x, y));
            }
         }
      }
      paintPanel.addPixels(blackPixels);
   }

   public static void main(String[] args) {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            new Main();
         }
      });
   }
}
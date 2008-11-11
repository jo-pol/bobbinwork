/* ThreadStyleToolBar.java Copyright 2006-2007 by J. Falkink-Pol
 *
 * This file is part of BobbinWork.
 *
 * BobbinWork is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BobbinWork is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BobbinWork.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.BobbinWork.diagram.gui;

import static nl.BobbinWork.bwlib.gui.Localizer.applyStrings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;

import nl.BobbinWork.diagram.model.ThreadStyle;
import nl.BobbinWork.diagram.model.Twist;
import nl.BobbinWork.diagram.xml.XmlHandler;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public class ThreadStyleToolBar extends JToolBar {

    private Twist twist;

    ThreadStyle getStyleOfFrontThread() {
        return twist.getFront().getStyle();
    }

    private ThreadStyle getStyleOfBackThread() {
        return twist.getBack().getStyle();
    }

    // makes the style of the twist threads available
    private Preview preview = new Preview();

    private static XmlHandler xmlHandler;
    public static XmlHandler getXmlHandler() throws ParserConfigurationException, SAXException {
      if (xmlHandler == null )xmlHandler = new XmlHandler();
      return xmlHandler;
    }

    private class Preview extends JPanel {

        Preview() {

            int w = 12;
            int wx2 = w*2;
            Dimension dim = new Dimension(wx2, wx2);
            setPreferredSize(dim);
            setMaximumSize(dim);

            setBackground(new Color(0xFFFFFF));
            // setBorder(BorderFactory.createLoweredBevelBorder());
            // has side effects: increments together with coreSpinner

            try {
                Element el = getXmlHandler().getTwist(w).getDocumentElement();
                twist = new Twist(el);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void paintComponent(Graphics g) {

            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            twist.draw(g2, false, true);
        }

        private void update() {
            getStyleOfBackThread().set(getStyleOfFrontThread());
            preview.repaint();
        }
    }

    private JSpinner coreSpinner = new JSpinner(new SpinnerNumberModel //
            (getStyleOfFrontThread().getWidth(), 1, getStyleOfFrontThread().getBackGround().getWidth() - 2, 1));

    private JSpinner shadowSpinner = new JSpinner(new SpinnerNumberModel //
            (getStyleOfFrontThread().getBackGround().getWidth(), getStyleOfFrontThread().getWidth() + 2, 20,
                    2));

    private int getSpinnerValue(ChangeEvent e) {

        SpinnerNumberModel source = (SpinnerNumberModel) e.getSource();
        return Integer.parseInt(source.getValue().toString());
    }

    private abstract class ColorButton extends JButton implements ActionListener {

        ColorButton(Color color) {

            setBackground(color);
            Dimension dim = new Dimension(20, 20);
            setPreferredSize(dim);
            setMaximumSize(dim);

            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {

            JButton source = (JButton) e.getSource();
            Color color = JColorChooser.showDialog(this, this.getText(), source.getBackground());
            if (color != null) {
                source.setBackground(color);
                setThreadPen(color);
                preview.update();
            }
        }

        protected abstract void setThreadPen(Color color);
    }

    private ColorButton shadowButton = new ColorButton(getStyleOfFrontThread().getBackGround().getColor()) {
        protected void setThreadPen(Color color) {
            getStyleOfFrontThread().getBackGround().setColor(color);
        }
    };

    private ColorButton coreButton = new ColorButton(getStyleOfFrontThread().getColor()) {
        protected void setThreadPen(Color color) {
            getStyleOfFrontThread().setColor(color);
            if (shadowButton.getBackground().getRGB() == -1) {
                // once the shadow is white, it should stay white
                // so override the default brighter background set by the model
                getStyleOfFrontThread().getBackGround().setColor(shadowButton.getBackground());
            } else {
                // use the default brighter background set by the model
                shadowButton.setBackground(getStyleOfFrontThread().getBackGround().getColor());
            }
        }
    };

    void setStyleOfFrontThread(ThreadStyle p) {
        if (p != null) {
            getStyleOfFrontThread().set(p);
            coreButton.setBackground(p.getColor());
            ((SpinnerNumberModel) coreSpinner.getModel()).setValue(new Integer(p.getWidth()));
            shadowButton.setBackground(p.getBackGround().getColor());
            ((SpinnerNumberModel) shadowSpinner.getModel()).setValue(new Integer(p
                    .getBackGround().getWidth()));
            preview.update();
        }
        
    }
    
    ThreadStyleToolBar(ResourceBundle bundle) throws ParserConfigurationException {
        setFloatable(false);
        setRollover(true);
        setBorder(null);
        JButton hint = new JButton(new ImageIcon(ThreadStyleToolBar.class.getResource("styleTooltip.gif"))); //$NON-NLS-1$
        hint.setMinimumSize(new Dimension(0,0));
        //hint.setEnabled(false);
        
        // tooltips
        applyStrings(preview, "ThreadStyle"); //$NON-NLS-1$
        applyStrings(coreSpinner, "ThreadStyle_core_width"); //$NON-NLS-1$
        applyStrings(shadowSpinner, "ThreadStyle_shadow_width"); //$NON-NLS-1$
        applyStrings(coreButton, "ThreadStyle_core_color"); //$NON-NLS-1$
        applyStrings(shadowButton, "ThreadStyle_shadow_color"); //$NON-NLS-1$
        applyStrings(hint, "ThreadStyle_mouseHint"); //$NON-NLS-1$
 
        // dimensions
        Dimension dim = new Dimension(//
                (int) (coreSpinner.getPreferredSize().width * 1.4), //
                coreSpinner.getPreferredSize().height);
        coreSpinner.setMaximumSize(dim);
        shadowSpinner.setMaximumSize(dim);
        dim = new Dimension((int) dim.getHeight(), (int) dim.getHeight());
        coreButton.setMaximumSize(dim);
        shadowButton.setMaximumSize(dim);

        // put the components on the toolbar
        add(Box.createHorizontalStrut(3));
        add(coreButton);
        add(Box.createHorizontalStrut(3));
        add(coreSpinner);
        add(Box.createHorizontalStrut(6));
        add(shadowButton);
        add(Box.createHorizontalStrut(3));
        add(shadowSpinner);
        add(Box.createHorizontalStrut(6));
        add(preview);
        add(Box.createHorizontalStrut(0));
        add(hint);

        // the width spinners are mutually constrained and therefore should
        // listen to each other
        // on the flight these listeners keep the threadPen and preview
        // up-to-date

        coreSpinner.getModel().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {

                SpinnerNumberModel shadowModel = (SpinnerNumberModel) shadowSpinner.getModel();

                int i = getSpinnerValue(e);
                shadowModel.setMinimum(Integer.valueOf(i + 2));
                getStyleOfFrontThread().setWidth(i);

                // override the default background set by ThreadStyle
                i = shadowModel.getNumber().intValue();
                getStyleOfFrontThread().getBackGround().setWidth(i);

                preview.update();
            }
        });

        shadowSpinner.getModel().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {

                SpinnerNumberModel coreModel = (SpinnerNumberModel) coreSpinner.getModel();

                int i = getSpinnerValue(e);
                coreModel.setMaximum(Integer.valueOf(i - 2));
                getStyleOfFrontThread().getBackGround().setWidth(i);

                preview.update();
            }
        });

    }
}

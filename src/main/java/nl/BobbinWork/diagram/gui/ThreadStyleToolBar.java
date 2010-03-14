/* ThreadStyleToolBar.java Copyright 2006-2007 by J. Pol
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

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorChooserComponentFactory;
import javax.swing.event.*;
import javax.xml.parsers.ParserConfigurationException;

import nl.BobbinWork.bwlib.gui.Localizer;
import nl.BobbinWork.diagram.model.*;
import nl.BobbinWork.diagram.model.Point;

import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public class ThreadStyleToolBar
    extends JToolBar
{

  private static final int PREVIEW_WITH = 20;

  private final Switch twist = createTwist();

  // makes the style of the twist threads available
  private final Preview preview =
      new Preview( new Dimension( PREVIEW_WITH, PREVIEW_WITH ) );

  public ThreadStyle getCoreStyle()
  {
    return twist.getFronts()[0].getStyle();
  }

  private Style getShadowStyle()
  {
    return twist.getFronts()[0].getStyle().getShadow();
  }

  private int getCoreWidth()
  {
    return getCoreStyle().getWidth();
  }

  private int getShadowWidth()
  {
    return getShadowStyle().getWidth();
  }

  private void setCoreWidth(
      final int value)
  {
    twist.getFronts()[0].getStyle().setWidth( value );
    twist.getBacks()[0].getStyle().setWidth( value );
  }

  private void setShadowWidth(
      final int value)
  {
    twist.getFronts()[0].getStyle().getShadow().setWidth( value );
    twist.getBacks()[0].getStyle().getShadow().setWidth( value );
  }

  private void setCoreColor(
      final Color value)
  {
    twist.getFronts()[0].getStyle().setColor( value );
    twist.getBacks()[0].getStyle().setColor( value );
  }

  private void setShadowColor(
      final Color value)
  {
    twist.getFronts()[0].getStyle().getShadow().setColor( value );
    twist.getBacks()[0].getStyle().getShadow().setColor( value );
  }

  private class Preview
      extends JPanel
  {
    Preview(final Dimension dim)
    {

      setPreferredSize( dim );
      setMaximumSize( dim );

      setBackground( new Color( 0xFFFFFF ) );
      // setBorder(BorderFactory.createLoweredBevelBorder());
      // has side effects: increments together with coreSpinner
    }

    public void paintComponent(
        final Graphics g)
    {

      super.paintComponent( g );
      DiagramPainter.paint( (Graphics2D) g, twist.getThreads() );
    }
  }

  private final JSpinner coreSpinner = new JSpinner( new SpinnerNumberModel //
      ( getCoreWidth(), 1, getShadowWidth() - 2, 1 ) );

  private final JSpinner shadowSpinner = new JSpinner( new SpinnerNumberModel //
      ( getShadowWidth(), getCoreWidth() + 2, 20, 2 ) );

  private int getSpinnerValue(
      final ChangeEvent e)
  {

    final SpinnerNumberModel source = (SpinnerNumberModel) e.getSource();
    return Integer.parseInt( source.getValue().toString() );
  }

  public static AbstractColorChooserPanel findPanel(final JColorChooser chooser, final String name) {
    final AbstractColorChooserPanel[] panels = chooser.getChooserPanels();
    for (int i = 0; i < panels.length; i++) {
      final String clsName = panels[i].getClass().getName();
      if (clsName.equals(name)) {
        return panels[i];
      }
    }
    return null;
  }

  private abstract class ColorButton
      extends JButton
      implements ActionListener
  {

    private final JDialog dialog;

    ColorButton(final String iconFileName, String keyBase)
    {
      applyStrings(this,keyBase);
      final URL url = ThreadStyleToolBar.class.getResource( iconFileName );
      final String name = Localizer.getString(keyBase+"_dialog_title");
      final JColorChooser chooser = new JColorChooser();
      chooser.setChooserPanels( ColorChooserComponentFactory.getDefaultChooserPanels() );
      setIcon( new ImageIcon( url ) );
      addActionListener( this );
      setRequestFocusEnabled(false);
      
      ActionListener okListener = new ActionListener()
      {
        @Override
        public void actionPerformed(
            final ActionEvent e)
        {
          setColor( chooser.getColor() );
          preview.repaint();
        }
      };
      dialog =
          JColorChooser.createDialog( ThreadStyleToolBar.this, name, true,
              chooser, okListener, null );
    }

    public void actionPerformed(
        final ActionEvent e)
    {
      dialog.setVisible( true );
    }

    protected abstract Color getColor();

    protected abstract void setColor(
        Color color);
  }

  private final ColorButton shadowButton = new ColorButton( "back.PNG", "ThreadStyle_shadow_color" ) { //$NON-NLS-1$

        protected Color getColor()
        {
          return getShadowStyle().getColor();
        }

        protected void setColor(
            final Color color)
        {
          setShadowColor( color );
        }
      };

  private final ColorButton coreButton = new ColorButton( "front.PNG", "ThreadStyle_core_color" ) { //$NON-NLS-1$

        protected Color getColor()
        {
          return getCoreStyle().getColor();
        }

        protected void setColor(
            final Color color)
        {
          final Color shadowColor = getShadowStyle().getColor();
          final int shadowRGB = shadowColor.getRGB();
          setCoreColor( color );
          if (shadowRGB == -1) {
            // once the shadow is white, it should stay white
            // so override the default brighter background set by the model
            setShadowColor( new Color( -1 ) );
          }
        }
      };

  public void setCoreStyle(
      final ThreadStyle p)
  {
    if (p != null) {
      getCoreStyle().apply( p );
      twist.getBacks()[0].getStyle().apply( p );
      ((SpinnerNumberModel) coreSpinner.getModel()).setValue( Integer
          .valueOf( p.getWidth() ) );
      final Integer width = Integer.valueOf( p.getShadow().getWidth() );
      ((SpinnerNumberModel) shadowSpinner.getModel()).setValue( width );
      preview.repaint();
    }

  }

  public ThreadStyleToolBar()
      throws SAXException, IOException, ParserConfigurationException
  {

    setFloatable( false );

    // tooltips
    applyStrings( preview, "ThreadStyle" ); //$NON-NLS-1$
    applyStrings( coreSpinner, "ThreadStyle_core_width" ); //$NON-NLS-1$
    applyStrings( shadowSpinner, "ThreadStyle_shadow_width" ); //$NON-NLS-1$

    // dimensions
    final Dimension dim = new Dimension( //
        (int) (coreSpinner.getPreferredSize().width * 1.4), //
        coreSpinner.getPreferredSize().height );
    coreSpinner.setMaximumSize( dim );
    shadowSpinner.setMaximumSize( dim );

    // put the components on the toolbar
    // add(Box.createHorizontalStrut(3));
    add( preview );
    add( Box.createHorizontalStrut( 3 ) );
    add( coreButton );
    add( Box.createHorizontalStrut( 3 ) );
    add( coreSpinner );
    add( Box.createHorizontalStrut( 6 ) );
    add( shadowButton );
    add( Box.createHorizontalStrut( 3 ) );
    add( shadowSpinner );
    add( Box.createHorizontalStrut( 3 ) );

    // the width spinners are mutually constrained and therefore should
    // listen to each other
    // on the flight these listeners keep the threadPen and preview
    // up-to-date

    coreSpinner.getModel().addChangeListener( new ChangeListener()
    {

      public void stateChanged(
          final ChangeEvent e)
      {

        final SpinnerNumberModel shadowModel =
            (SpinnerNumberModel) shadowSpinner.getModel();

        int i = getSpinnerValue( e );
        shadowModel.setMinimum( Integer.valueOf( i + 2 ) );
        setCoreWidth( i );

        // override the default shadow set by ThreadStyle
        i = shadowModel.getNumber().intValue();
        setShadowWidth( i );

        preview.repaint();
      }
    } );

    shadowSpinner.getModel().addChangeListener( new ChangeListener()
    {

      public void stateChanged(
          final ChangeEvent e)
      {

        final SpinnerNumberModel coreModel =
            (SpinnerNumberModel) coreSpinner.getModel();

        final int i = getSpinnerValue( e );
        coreModel.setMaximum( Integer.valueOf( i - 2 ) );
        setShadowWidth( i );

        preview.repaint();
      }
    } );

  }

  private static Twist createTwist()
  {
    final double w = PREVIEW_WITH;
    final Point east = new Point( 0D, w / 2D );
    final Point west = new Point( w, w / 2D );
    final Point north = new Point( w / 2D, 0 );
    final Point south = new Point( w / 2D, w );
    final ThreadSegment[] back = new ThreadSegment[]{new ThreadSegment( east, null, null, west )};
    final ThreadSegment[] front = new ThreadSegment[]{new ThreadSegment( north, null, null, south )};
    return new Twist( new Range( 1, 2 ), front, back );
  }
}

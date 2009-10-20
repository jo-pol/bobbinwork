/* BWTree.java Copyright 2006-2008 by J. Pol
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
package nl.BobbinWork.viewer.gui;

import java.awt.Component;

import javax.swing.*;
import javax.swing.tree.*;

import nl.BobbinWork.diagram.model.*;

public class VoodooTree
    extends JTree
{
  private static class Renderer
      extends DefaultTreeCellRenderer
  {
    public Component getTreeCellRendererComponent(
        final JTree tree,
        final Object value,
        final boolean sel,
        final boolean expanded,
        final boolean leaf,
        final int row,
        final boolean hasFocus)
    {
      super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf,
          row, hasFocus );

      Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
      if (userObject == null) return this;

      if (userObject instanceof MultiplePairsPartition) {
        decorate( (MultiplePairsPartition) userObject );
      } else if (userObject instanceof Switch) {
        decorate( (Switch) userObject );
      }
      return this;
    }

    private void decorate(
        MultipleThreadsPartition p)
    {
      setToolTipText( p.getTooltip() );
      setText( p.getCaption() );
      setIcon( p.getIcon() );
    };
  };

  public VoodooTree(final Diagram diagram)
  {
    super( new DefaultMutableTreeNode( "root" ) );
    ToolTipManager.sharedInstance().registerComponent( this );
    setShowsRootHandles( false );
    setRootVisible( true );
    setCellRenderer( new Renderer() );
    setSelectionMode();
    buildTree( getRoot(), diagram );
    ((DefaultTreeModel) getModel()).nodeStructureChanged( getRoot() );
  }

  private void buildTree(
      final DefaultMutableTreeNode treeNode,
      final Partition partition)
  {
    if (partition instanceof MultiplePairsPartition) {
      for (final Partition p : ((MultiplePairsPartition) partition)
          .getPartitions()) {
        final DefaultMutableTreeNode child = new DefaultMutableTreeNode( p );
        treeNode.add( child );
        buildTree( child, p );
      }
    }
  }

  private void setSelectionMode()
  {
    getSelectionModel().setSelectionMode(
        TreeSelectionModel.SINGLE_TREE_SELECTION );
  }

  private DefaultMutableTreeNode getRoot()
  {
    final DefaultTreeModel treeModel = (DefaultTreeModel) getModel();
    return (DefaultMutableTreeNode) treeModel.getRoot();
  }
}

/* BWTree.java Copyright 2009 by J. Pol
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

import java.awt.Component;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import nl.BobbinWork.diagram.model.*;
import nl.BobbinWork.diagram.xml.DiagramRebuilder;

public class DiagramTree
    extends JTree
{
  private static final int INITIAL_CAPACITY = 5000;
  private final Map<MultipleThreadsPartition, DefaultMutableTreeNode> map =
      new HashMap<MultipleThreadsPartition, DefaultMutableTreeNode>(
          INITIAL_CAPACITY );

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

      final Object userObject =
          ((DefaultMutableTreeNode) value).getUserObject();
      if (userObject == null) return this;

      if (userObject instanceof Partition) {
        decorate( (Partition) userObject );
      }
      return this;
    }

    private void decorate(
        final Partition p)
    {
      String caption = p.getCaption();
      if (!p.isVisible()) {
        caption = "<html><s>" + caption + "</s></html>";
      }
      if (!DiagramRebuilder.canReplace( p )) {
        caption = "<html><em>" + caption + "</em></html>";
      }
      if (DiagramRebuilder.canCopy( p )) {
        caption = "<html><strong>" + caption + "</stronng></html>";
      }
      setText( caption );
      if (p.getIcon() != null) setIcon( p.getIcon() );
      if (p.getTooltip() != null) setToolTipText( p.getTooltip() );
    };
  };

  public DiagramTree()
  {
    super( new DefaultMutableTreeNode( null ) );
    ToolTipManager.sharedInstance().registerComponent( this );
    setShowsRootHandles( false );
    setRootVisible( true );
    setCellRenderer( new Renderer() );
    setSelectionMode();
  }

  void setDiagramModel(
      final Diagram diagram)
  {
    final DefaultTreeModel treeModel = (DefaultTreeModel) getModel();
    final DefaultMutableTreeNode root =
        (DefaultMutableTreeNode) treeModel.getRoot();
    root.removeAllChildren();
    root.setUserObject( diagram );
    map.clear();
    buildTree( root, diagram );
    treeModel.nodeStructureChanged( root );
  }

  private void buildTree(
      final DefaultMutableTreeNode treeNode,
      final Partition partition)
  {
    if (partition instanceof MultiplePairsPartition) {
      final MultiplePairsPartition ps = (MultiplePairsPartition) partition;
      for (final Partition p : ps.getPartitions()) {
        final DefaultMutableTreeNode child = new DefaultMutableTreeNode( p );
        treeNode.add( child );
        buildTree( child, p );
        if (p instanceof MultipleThreadsPartition)
          map.put( (MultipleThreadsPartition) p, child );
      }
    }
  }

  void select(
      final MultipleThreadsPartition p)
  {
    if (p == null) return;
    final DefaultMutableTreeNode node = map.get( p );
    if (node == null) return;

    final TreePath path = new TreePath( node.getPath() );
    setSelectionPath( path );
    scrollPathToVisible( path );
  }

  private void setSelectionMode()
  {
    getSelectionModel().setSelectionMode(
        TreeSelectionModel.SINGLE_TREE_SELECTION );
  }
}
/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.worldviewer.core;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.lang.reflect.Field;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;

import org.terasology.rendering.nui.properties.Checkbox;
import org.terasology.rendering.nui.properties.OneOf.Enum;
import org.terasology.worldviewer.config.FacetConfig;
import org.terasology.worldviewer.gui.UIBindings;
import org.terasology.worldviewer.layers.FacetLayer;

/**
 * The facet layer configuration panel (at the left)
 * @author Martin Steiger
 */
public class FacetPanel extends JPanel {

    private static final long serialVersionUID = -4395448394330407251L;

    private final JPanel configPanel;

    public FacetPanel(List<FacetLayer> facets) {
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        TableModel listModel = new FacetTableModel(facets);
        JTable facetList = new JTable(listModel);

        for (FacetLayer facetLayer : facets) {
            facetLayer.addObserver(layer -> facetList.repaint());
        }

        facetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        facetList.setTransferHandler(new TableRowTransferHandler(facetList));
        facetList.setDropMode(DropMode.INSERT_ROWS);
        facetList.setDragEnabled(true);
        facetList.getColumnModel().getColumn(0).setMaxWidth(25);
        facetList.getColumnModel().getColumn(0).setResizable(false);
        facetList.getTableHeader().setReorderingAllowed(false);
        add(facetList.getTableHeader(), gbc.clone());
        gbc.gridy++;
        add(facetList, gbc.clone());

        JLabel listInfoText = new JLabel("Drag layers to change rendering order");
        listInfoText.setAlignmentX(0.5f);
        gbc.gridy++;
        add(listInfoText, gbc.clone());

        configPanel = new JPanel();
        configPanel.setBorder(BorderFactory.createTitledBorder("Config"));
        CardLayout cardLayout = new CardLayout();
        configPanel.setLayout(cardLayout);
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.top = 10;
        add(configPanel, gbc.clone());

        for (FacetLayer layer : facets) {
            configPanel.add(createConfigs(layer), Integer.toString(System.identityHashCode(layer)));
        }

        facetList.getSelectionModel().addListSelectionListener(e -> {
            int selIdx = facetList.getSelectedRow();
            if (selIdx > -1) {
                FacetLayer layer = facets.get(selIdx);
                String id = Integer.toString(System.identityHashCode(layer));
                cardLayout.show(configPanel, id);
            }
        });
    }

    protected JPanel createConfigs(FacetLayer layer) {
        JPanel panelWrap = new JPanel(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));

        FacetConfig config = layer.getConfig();
        if (config != null) {
            for (Field field : config.getClass().getDeclaredFields()) {

                if (field.getAnnotations().length > 0) {
                    field.setAccessible(true);

                    processAnnotations(panel, layer, field);
                }
            }
        }

        panel.setBorder(new EmptyBorder(0, 5, 0, 0));
        panelWrap.add(panel, BorderLayout.NORTH);
        return panelWrap;
    }

    private void processAnnotations(JPanel panel, FacetLayer layer, Field field) {
        FacetConfig config = layer.getConfig();
        JSpinner spinner = UIBindings.processRangeAnnotation(config, field);
        if (spinner != null) {
            spinner.addChangeListener(event -> layer.notifyObservers());

            JLabel label = new JLabel(spinner.getName());
            label.setToolTipText(spinner.getToolTipText());

            panel.add(label);
            panel.add(spinner);
        }

        JCheckBox checkbox = UIBindings.processCheckboxAnnotation(layer, field, "visible");
        if (checkbox != null) {
            JLabel label = new JLabel(checkbox.getName());
            label.setToolTipText(checkbox.getToolTipText());

            panel.add(label);
            panel.add(checkbox);
        }

        JComboBox<?> combo = UIBindings.processEnumAnnotation(layer, field);
        if (combo != null) {
            JLabel label = new JLabel(combo.getName());
            label.setToolTipText(combo.getToolTipText());

            panel.add(label);
            panel.add(combo);
        }
    }
}

/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.command.toolbar.processes;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.ui.dropdown.BaseListItem;
import org.eclipse.che.ide.ui.dropdown.DropDownList;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link ProcessesListView} that displays processes in a drop down list.
 */
@Singleton
public class ProcessesListViewImpl implements ProcessesListView {

    private final FlowPanel    rootPanel;
    private final DropDownList dropDownList;

    private final Map<Process, BaseListItem<Process>> listItems;
    private final Map<Process, ProcessItemRenderer>   renderers;

    private ActionDelegate delegate;

    @Inject
    public ProcessesListViewImpl(CommandResources resources) {
        listItems = new HashMap<>();
        renderers = new HashMap<>();

        final Label label = new Label("EXEC");
        label.addStyleName(resources.commandToolbarCss().processesListLabel());

        final InlineHTML emptyListWidget = new InlineHTML("<b>Ready</b> - start command");
        emptyListWidget.addStyleName(resources.commandToolbarCss().processesListItemText());

        dropDownList = new DropDownList(emptyListWidget);
        dropDownList.addStyleName(resources.commandToolbarCss().processesList());
        dropDownList.setSelectionHandler(item -> {
            for (Map.Entry<Process, BaseListItem<Process>> entry : listItems.entrySet()) {
                if (item.equals(entry.getValue())) {
                    delegate.onProcessChosen(entry.getKey());
                }
            }
        });

        rootPanel = new FlowPanel();
        rootPanel.add(label);
        rootPanel.add(dropDownList);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    @Override
    public void clearList() {
        dropDownList.clear();

        // TODO: set `empty list widget` to the dropdown list's header
    }

    @Override
    public void notifyProcessStopped(Process process) {
        final ProcessItemRenderer renderer = renderers.get(process);

        if (renderer != null) {
            renderer.notifyProcessStopped();
        }
    }

    @Override
    public void addProcess(Process process) {
        final BaseListItem<Process> listItem = new BaseListItem<>(process);
        final ProcessItemRenderer renderer = new ProcessItemRenderer(listItem,
                                                                     p -> delegate.onStopProcess(p),
                                                                     p -> delegate.onReRunProcess(p));

        listItems.put(process, listItem);
        renderers.put(process, renderer);

        dropDownList.addItem(listItem, renderer);
    }

    @Override
    public void removeProcess(Process process) {
        final BaseListItem<Process> listItem = listItems.get(process);

        if (listItem != null) {
            listItems.remove(process);
            renderers.remove(process);

            dropDownList.removeItem(listItem);
        }
    }
}
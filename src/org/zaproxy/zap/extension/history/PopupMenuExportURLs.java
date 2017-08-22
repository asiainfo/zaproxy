/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.history;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.view.widgets.WritableFileChooser;

public class PopupMenuExportURLs extends ExtensionPopupMenuItem {

    private static final long serialVersionUID = 1L;
    protected ExtensionHistory extension = null;

    private static Logger log = Logger.getLogger(PopupMenuExportURLs.class);

    public PopupMenuExportURLs(String menuItem) {
        super(menuItem);

        this.addActionListener(new java.awt.event.ActionListener() { 

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                performAction();
            }
        });

    }
    
    protected void performAction() {
        File file = getOutputFile();
        if (file == null) {
            return;
        }
        writeURLs(file, getOutputSet(
                (SiteNode) extension.getView().getSiteTreePanel().getTreeSite().getModel().getRoot()));
    }

    public void setExtension(ExtensionHistory extension) {
        this.extension = extension;
    }

    protected SortedSet<String> getOutputSet(SiteNode startingPoint) {
        SortedSet<String> outputSet = new TreeSet<String>();
        Enumeration<?> en = (startingPoint.preorderEnumeration());
        while (en.hasMoreElements()) {
            SiteNode node = (SiteNode) en.nextElement();
            if (node.isRoot()) {
                continue;
            }
            HistoryReference nodeHR = node.getHistoryReference();
            if (nodeHR != null
                    && !HistoryReference.getTemporaryTypes().contains(nodeHR.getHistoryType())) {
                outputSet.add(nodeHR.getURI().toString());
            }
        }
        return outputSet;
    }

    protected void writeURLs(File file, SortedSet<String> aSet) {
    
        boolean html = file.getName().toLowerCase().endsWith(".htm") || file.getName().toLowerCase().endsWith(".html");
        
        BufferedWriter fw = null;
        try {
            fw = new BufferedWriter(new FileWriter(file, false));

            for (String item : aSet) {
                item = html ? wrapHTML(item) : item;
                fw.write(item);
                fw.newLine();
            }

        } catch (Exception e1) {
            log.warn(e1.getStackTrace(), e1);
            extension.getView().showWarningDialog(Constant.messages.getString("file.save.error") + file.getAbsolutePath());
        } finally {
            try {
                fw.close();
            } catch (Exception e2) {
                log.warn(e2.getStackTrace(), e2);
            }
        }
    }
    
    private String wrapHTML(String input) {
        StringBuilder sb = new StringBuilder(50);
        sb.append("<a href=\"").append(input).append("\">");
        sb.append(input).append("</a><br>");

        return sb.toString();
    }
    
    protected File getOutputFile() {
        WritableFileChooser chooser = new WritableFileChooser(extension.getModel().getOptionsParam().getUserDirectory());
        FileNameExtensionFilter textFilesFilter = new FileNameExtensionFilter(Constant.messages.getString("file.format.ascii"), "txt");
        FileNameExtensionFilter htmlFilesFilter = new FileNameExtensionFilter(Constant.messages.getString("file.format.html"), "html", "htm");

        chooser.addChoosableFileFilter(textFilesFilter);
        chooser.addChoosableFileFilter(htmlFilesFilter);
        chooser.setFileFilter(textFilesFilter);
        
        File file = null;
        int rc = chooser.showSaveDialog(extension.getView().getMainFrame());
        if(rc == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            if (file == null) {
                return file;
            }
            extension.getModel().getOptionsParam().setUserDirectory(chooser.getCurrentDirectory());
    		String fileNameLc = file.getAbsolutePath().toLowerCase();
    		if (! fileNameLc.endsWith(".txt") && ! fileNameLc.endsWith(".htm") &&
    				! fileNameLc.endsWith(".html")) {
    		    String ext;
    		    if (htmlFilesFilter.equals(chooser.getFileFilter())) {
    		        ext = ".html";
    		    } else {
    		        ext = ".txt";
    		    }
    		    file = new File(file.getAbsolutePath() + ext);
    		}
    		return file;
    		
	    }
	    return file;
    }

}

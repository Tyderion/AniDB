/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GUI_Tab_Options_AddFoldersOnStartup.java
 *
 * Created on 29.06.2009, 13:11:04
 */

package gui;

/**
 *
 * @author Arokh
 */
public class GUI_Options_AddFoldersOnStartup extends javax.swing.JPanel {
    private IGUI gui;

    /** Creates new form GUI_Tab_Options_AddFoldersOnStartup */
    public GUI_Options_AddFoldersOnStartup() {
        initComponents();
    }
    public GUI_Options_AddFoldersOnStartup(IGUI gui) {
        this();
        this.gui = gui;

        chck_AddFolderOnStartup.setSelected((Boolean)gui.FromMem("AddOnLoad", false));
        txt_FoldersToAdd.setText((String)gui.FromMem("FolderToAddOnLoad", ""));
        ToggleFolderAutoAdd();
    }

    protected void ToggleFolderAutoAdd(){
        gui.ToMem("AddOnLoad", chck_AddFolderOnStartup.isSelected());
        txt_FoldersToAdd.setVisible(chck_AddFolderOnStartup.isSelected());
        txt_FoldersToAdd.setEnabled(chck_AddFolderOnStartup.isSelected());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnl_AddFolderOnStartup = new javax.swing.JPanel();
        chck_AddFolderOnStartup = new javax.swing.JCheckBox();
        scrl_txt_FoldersToAdd = new javax.swing.JScrollPane();
        txt_FoldersToAdd = new javax.swing.JTextArea();

        pnl_AddFolderOnStartup.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        pnl_AddFolderOnStartup.setOpaque(false);

        chck_AddFolderOnStartup.setText("Add following folders on Startup:");
        chck_AddFolderOnStartup.setOpaque(false);
        chck_AddFolderOnStartup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chck_AddFolderOnStartupActionPerformed(evt);
            }
        });

        txt_FoldersToAdd.setColumns(20);
        txt_FoldersToAdd.setFont(new java.awt.Font("Tahoma", 0, 10));
        txt_FoldersToAdd.setRows(5);
        txt_FoldersToAdd.setEnabled(false);
        txt_FoldersToAdd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txt_FoldersToAddKeyReleased(evt);
            }
        });
        scrl_txt_FoldersToAdd.setViewportView(txt_FoldersToAdd);

        javax.swing.GroupLayout pnl_AddFolderOnStartupLayout = new javax.swing.GroupLayout(pnl_AddFolderOnStartup);
        pnl_AddFolderOnStartup.setLayout(pnl_AddFolderOnStartupLayout);
        pnl_AddFolderOnStartupLayout.setHorizontalGroup(
            pnl_AddFolderOnStartupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_AddFolderOnStartupLayout.createSequentialGroup()
                .addComponent(chck_AddFolderOnStartup)
                .addContainerGap(81, Short.MAX_VALUE))
            .addComponent(scrl_txt_FoldersToAdd, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
        );
        pnl_AddFolderOnStartupLayout.setVerticalGroup(
            pnl_AddFolderOnStartupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_AddFolderOnStartupLayout.createSequentialGroup()
                .addComponent(chck_AddFolderOnStartup)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrl_txt_FoldersToAdd, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnl_AddFolderOnStartup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnl_AddFolderOnStartup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void chck_AddFolderOnStartupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chck_AddFolderOnStartupActionPerformed
        ToggleFolderAutoAdd();
}//GEN-LAST:event_chck_AddFolderOnStartupActionPerformed

    private void txt_FoldersToAddKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_FoldersToAddKeyReleased
        gui.ToMem("FolderToAddOnLoad", txt_FoldersToAdd.getText());
}//GEN-LAST:event_txt_FoldersToAddKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JCheckBox chck_AddFolderOnStartup;
    protected javax.swing.JPanel pnl_AddFolderOnStartup;
    protected javax.swing.JScrollPane scrl_txt_FoldersToAdd;
    protected javax.swing.JTextArea txt_FoldersToAdd;
    // End of variables declaration//GEN-END:variables

}

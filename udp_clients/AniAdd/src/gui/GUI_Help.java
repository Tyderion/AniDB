/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GUI_Help.java
 *
 * Created on 29.06.2009, 21:05:33
 */

package gui;

import aniAdd.Communication.ComEvent;
import aniAdd.IAniAdd;
import aniAdd.misc.Mod_Memory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author Arokh
 */
public class GUI_Help extends javax.swing.JPanel implements GUI.ITab {
    Mod_Memory mem;

    /** Creates new form GUI_Help */
    public GUI_Help() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbl_Icons = new javax.swing.JLabel();
        ctrl_IconHelp = new gui.GUI_Help_Icons();
        btn_ResetSettings = new javax.swing.JButton();

        lbl_Icons.setFont(new java.awt.Font("Tahoma", 1, 14));
        lbl_Icons.setText("Icons:");

        btn_ResetSettings.setText("Reset Settings to Default");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbl_Icons)
                    .addComponent(ctrl_IconHelp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_ResetSettings))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbl_Icons)
                .addGap(2, 2, 2)
                .addComponent(ctrl_IconHelp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btn_ResetSettings)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_ResetSettings;
    protected gui.GUI_Help_Icons ctrl_IconHelp;
    protected javax.swing.JLabel lbl_Icons;
    // End of variables declaration//GEN-END:variables

    public String TabName() { return "Help"; }
    public int PreferredTabLocation() { return 3; }

    public void Initialize(IAniAdd aniAdd, final IGUI gui) {
        mem = (Mod_Memory)aniAdd.GetModule("Memory");

        btn_ResetSettings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mem.clear();
            }
        });
    }

    public void Terminate() {}

    public void GUIEventHandler(ComEvent comEvent) {}

    public void GainedFocus() {}

    public void LostFocus() {}

}

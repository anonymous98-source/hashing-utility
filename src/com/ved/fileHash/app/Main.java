package com.ved.filehash.app;

import javax.swing.SwingUtilities;
import com.kpmg.filehash.ui.FileHashGenerator;



class Main{

     public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new FileHashGenerator().setVisible(true)
        );
    }

}

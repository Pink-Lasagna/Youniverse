package ru.jaromirchernyavsky.youniverse.custom;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DeleteConfirmation {
    public static void show(Context context, DialogInterface.OnClickListener ocl){
        boolean result = false;
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context).setTitle("Подтверждение").setMessage("Вы точно хотите удалить это?");
        dialogBuilder.setPositiveButton("Да", ocl);
        dialogBuilder.setNegativeButton("Нет", (dialogInterface, i) -> {});
        dialogBuilder.show();
    }
}

package com.example.kangwenn.currexez;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class accNumEnterDialog extends AppCompatDialogFragment {

    private EditText mAccNum;
    private String bankName;
    private dialogListener listener;

    public accNumEnterDialog() {
    }

    public accNumEnterDialog(String bankName) {
        this.bankName = bankName;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_bankaccountdialog, null);

        builder.setView(view)
                .setTitle("Enter bank account number"+ "("+bankName+")")
                .setPositiveButton("Comfirm",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String accNum = mAccNum.getText().toString();
                        if (accNum.length() > 20){
                            Toast.makeText(getActivity(), "Cannot more then 20 digit", Toast.LENGTH_SHORT).show();
                        }else if(accNum.length() < 5){
                            Toast.makeText(getActivity(), "Cannot less then 5 digit", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            listener.storeToDatabase(bankName,accNum);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        mAccNum = view.findViewById(R.id.editAccNum);

        return builder.create();
    }

    public void setBankName(String bankName){
        bankName = this.bankName;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (dialogListener) context;
        } catch (ClassCastException e) {
        }
    }

    public interface dialogListener{
        void storeToDatabase(String bank, String accountNum);
    }
}

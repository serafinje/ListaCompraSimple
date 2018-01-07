package com.sera.android.shoplist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

public class SendFeedbackActivity extends Activity 
{
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        InicializaUI();
    }
	
	
	private void InicializaUI()
	{
		setContentView(R.layout.send_feedback);
		
		RatingBar r = (RatingBar)findViewById(R.id.rbFeedbackScore);
		r.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
			public void onRatingChanged(RatingBar ratingBar, float rating,boolean fromUser) {
				TextView tvFeedbackScore = (TextView)findViewById(R.id.tvFeedbackScore);
				String txtrating = "";
				if (rating<6) txtrating = getResources().getString(R.string.strFeedbackScore5);
				if (rating<5) txtrating = getResources().getString(R.string.strFeedbackScore4);
				if (rating<4) txtrating = getResources().getString(R.string.strFeedbackScore3);
				if (rating<3) txtrating = getResources().getString(R.string.strFeedbackScore2);
				if (rating<2) txtrating = getResources().getString(R.string.strFeedbackScore1);
				
				tvFeedbackScore.setText(txtrating);
			}
		});
		
		// Al arrancar estar? vac?o este campo
		TextView tvFeedbackScore = (TextView)findViewById(R.id.tvFeedbackScore);
		tvFeedbackScore.setText("");
			
		// Env?o de correo
		Button btnSend = (Button)findViewById(R.id.btnSend);
		btnSend.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String texto="";
				RatingBar r = (RatingBar)findViewById(R.id.rbFeedbackScore);
				if (r.getNumStars()>0) {
					texto += "Puntuaci?n: " + r.getRating();
					
					TextView tvFeedbackScore = (TextView)findViewById(R.id.tvFeedbackScore);
					texto += " ("+tvFeedbackScore.getText().toString()+")";
				}
				
				EditText feedback = (EditText)findViewById(R.id.editFeedback);
				texto += "\nMensaje:\n" + feedback.getText().toString();
				
				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				emailIntent.setType("message/rfc822");
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ "sera3d@gmail.com"});
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, texto);
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			}
		});
		
	}
}

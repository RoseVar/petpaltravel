package com.petpal.petpaltravel.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.petpal.petpaltravel.R;
import com.petpal.petpaltravel.model.CompanionForPet;
import com.petpal.petpaltravel.model.PPTModel;

import java.text.SimpleDateFormat;

public class ShowDemandActivity extends AppCompatActivity {
    //Atributes
    TextView namePet, OriginCity, Destination, typePet, dateFrom, dateUntill, comments, nameLabel;
    String nameUser;
    String phoneUser;
    Button offerMe;
    PPTModel myModel;
    CompanionForPet myDemand;
    int idDemand;
    Boolean isShelter;
    int userId;
    View.OnClickListener listener;
    int situationFlag=0; // 0= normal, -1= missing phone, -2= applied -3=no more application accepted

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //recover urgent data
        recoverShared();
        if (isShelter){
            setContentView(R.layout.viewdemandasshelter_layout);
        } else {
            setContentView(R.layout.viewdemandasperson_layout);
        }
        //instantiate model
        myModel = new PPTModel();
        //recover needed data
        recoverDemandId();
        myDemand= myModel.recoverDemandById(idDemand);
        //Create view elements in activity
        initElements();
        //create a listener
        createListener();
        //load Data in view
        loadData();
        //add listener to elements
        addElementsToListener();
    }

    /**
     * Method for recovering data needed by bundle
     */
    private void recoverDemandId() {
        Bundle bun = this.getIntent().getExtras();
        idDemand= bun.getInt("idDemand",0);
    }

    /**
     * Method for create elements of activity
     */
    private void initElements() {
        namePet= (TextView) findViewById(R.id.etNombreMascota);
        OriginCity= (TextView) findViewById(R.id.etCiudadOrigenMascota);
        Destination= (TextView) findViewById(R.id.etCiudadDestinoMascota);
        typePet= (TextView) findViewById(R.id.etTipoMascota);
        dateFrom= (TextView) findViewById(R.id.etDisponibleDesde);
        dateUntill= (TextView) findViewById(R.id.etDisponibleHasta);
        comments= (TextView) findViewById(R.id.etComentarios);
        nameLabel= (TextView) findViewById(R.id.etNombrePersona);
        //set value to name of the user field
        nameLabel.setText(nameUser);
        offerMe= (Button) findViewById(R.id.btApadrinarMascota);
        //set situation flag depending on the case
        if (myDemand.getIdUserPersonInterested1()==userId | myDemand.getIdUserPersonInterested2()==userId |
                myDemand.getIdUserPersonInterested3()==userId){
            situationFlag=-2;
        } else if (myDemand.getIdUserPersonInterested1()!=0 & myDemand.getIdUserPersonInterested2()!=0 &
                myDemand.getIdUserPersonInterested3()!=0){
            situationFlag=-3;
        } else if (phoneUser==null) {
            situationFlag=-1;
        }
        //set value of text of offerme button
        setButtonOfferMe();
    }

    /**
     * Method for setting the value of text in offerme button
     * depending on the situation flag value
     */
    private void setButtonOfferMe() {
        //if we are the shelter that post this demand...
        if (isShelter & userId!=0 & userId==myDemand.getIdeUserShelterOffering()) {
            offerMe.setText("Modificar petición");
        //if not...
        } else {
            //depending on the situation flag
            switch (situationFlag) {
                case 0: //normal case
                    offerMe.setText("¡Quiero acompañarle!");
                    offerMe.setEnabled(true);
                    offerMe.setTextColor(Color.WHITE);
                    break;
                case -1: // missing phone
                    offerMe.setText("Falta tu teléfono para ofrecerte");
                    offerMe.setEnabled(true);
                    offerMe.setTextColor(Color.RED);
                    break;
                case -2: //person has applied already
                    offerMe.setText("Ya te has ofrecido");
                    offerMe.setEnabled(true);
                    offerMe.setTextColor(Color.WHITE);
                    break;
                case -3: //no more application accepted
                    offerMe.setText("Estamos a tope de personas voluntarias");
                    offerMe.setEnabled(false);
                    break;
                case -4: //there is some trouble
                    offerMe.setText("Prueba más tarde");
                    offerMe.setTextColor(Color.RED);
                    offerMe.setEnabled(true);
                    break;
            }
        }
    }

    /**
     * Method for recovering interesting data by Shared Preferences
     */
    private void recoverShared() {
        //Create shared prefereces object of a Shared preferences created
        SharedPreferences shared = getSharedPreferences("dades", MODE_PRIVATE);
        //if exist
        if (shared!=null) {
            //Use the editor to catch the couples of dates
            nameUser = shared.getString("userName", "");
            userId= shared.getInt("id", 0);
            isShelter = shared.getBoolean("isShelter", false);
            phoneUser= shared.getString("userPhone", null);
        }
    }

    /**
     * Method for creating a listener and identify what to do
     */
    private void createListener() {
        listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if we are the shelter that post this demand, open modify demand activity
                if (isShelter & userId!=0 & userId==myDemand.getIdeUserShelterOffering()){
                    //Intent intent  = new Intent(ShowDemandActivity.this, MODIFICAR_DEMANDA.class);
                    //startActivity(intent);
                //if not...
                } else {
                    switch (situationFlag) {
                        case 0: //normal case: person wants to apply to the demand
                        case -2: //person has applied already: person wants to un-apply the demand
                            Intent intent1  = new Intent(ShowDemandActivity.this, ApplyForDemand.class);
                            //Create a bundle object
                            Bundle bundle = new Bundle();
                            //set interesting data
                            bundle.putInt("idDemand", idDemand);
                            intent1.putExtras(bundle);
                            startActivity(intent1);
                            break;
                        case -1: // missing phone: open activity to go to change account details
                            Intent intent  = new Intent(ShowDemandActivity.this, ViewAccountActivity.class);
                            startActivity(intent);
                            break;
                    }
                }
            }
        };
    }

    /**
     * Method for adding listener to the elements
     */
    private void addElementsToListener() {
        offerMe.setOnClickListener (listener);
    }



    /**
     * Method for loading demand data in the view
     */
    private void loadData() {
        //if the demand is null (does not arrived to this activity), notify
        if(myDemand==null){
            namePet.setText("No encontrado");
            OriginCity.setText("No encontrado");
            Destination.setText("No encontrado");
            typePet.setText("No encontrado");
            dateFrom.setText("No encontrado");
            dateUntill.setText("No encontrado");
            comments.setText("No encontrado");
            offerMe.setText("No se ha encontrado");
            offerMe.setEnabled(false);
        //if there is a demand
        } else {
            namePet.setText(myDemand.getNamePet());
            OriginCity.setText(myDemand.getOriginCity());
            Destination.setText(myDemand.getDestinyCity());
            typePet.setText(myDemand.getTypePet());
            dateFrom.setText(new SimpleDateFormat("dd-MM-yyyy").format(myDemand.getAvailableFrom().getTime()));
            if (myDemand.getDeadline()!=null) {
                dateUntill.setText(new SimpleDateFormat("dd-MM-yyyy").format(myDemand.getDeadline().getTime()));
            } else {
                dateUntill.setText("Sin fecha límite");
            }
            comments.setText(myDemand.getComments());
        }
    }

    /**
     * Method for creating items of menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (isShelter) {
            menu.add(0, 1, 0, "Mi perfil");
            menu.add(0, 2, 1, "Ver mis peticiones");
            menu.add(0, 3, 2, "Publicar petición");
            menu.add(0, 4, 3, "Salir");
        } else {
            menu.add(0, 1, 0, "Mi perfil");
            menu.add(0, 2, 1, "Ver mis ofertas");
            menu.add(0, 3, 2, "Publicar oferta");
            menu.add(0, 4, 3, "Salir");
        }
        return true;
    }

    // Handles item selections from Option MENU
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case 1:
                    //Go to view account activity
                    Intent intent1 = new Intent(ShowDemandActivity.this, ViewAccountActivity.class);
                    startActivity(intent1);
                    break;
                case 2:
                    //If is Shelter, go to show my demands activity
                    if(isShelter) {
                        Intent intent2 = new Intent(ShowDemandActivity.this, SearchDemandsActivity.class);
                        startActivity(intent2);
                    //if is person, go to show my details activity
                    } else {
                        Intent intent2 = new Intent(ShowDemandActivity.this, SearchOffersActivity.class);
                        startActivity(intent2);
                    }
                    break;
                case 3:
                    //If is Shelter, go to add a demands activity
                    if (isShelter) {
                        Intent intent3 = new Intent(ShowDemandActivity.this, AddDemandActivity.class);
                        startActivity(intent3);
                    //if is person, go to add an offer activity
                    } else {
                        Intent intent3 = new Intent(ShowDemandActivity.this, AddOfferActivity.class);
                        startActivity(intent3);
                    }
                    break;
                case 4://Exit
                    finish();
                    break;
            }
        return true;
    }
}

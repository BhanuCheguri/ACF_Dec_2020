package com.anticorruptionforce.acf.utilities;

import android.content.Context;
import android.location.Geocoder;
import android.text.TextUtils;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Address;
import com.google.api.services.people.v1.model.Birthday;
import com.google.api.services.people.v1.model.Date;
import com.google.api.services.people.v1.model.Gender;
import com.google.api.services.people.v1.model.Person;
import com.anticorruptionforce.acf.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class PeopleApi {
    public static final String CONTACT_SCOPE = "https://www.googleapis.com/auth/contacts.readonly";
    public static final String BIRTHDAY_SCOPE = "https://www.googleapis.com/auth/user.birthday.read";
    private static PeopleService mInstance;

    private static PeopleService getService() {
        if (mInstance == null) mInstance = initializeService();
        return mInstance;
    }

    private static PeopleService initializeService() {
        Context context = App.getAppContext();
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(context, Arrays.asList(CONTACT_SCOPE, BIRTHDAY_SCOPE));
        credential.setSelectedAccount(GoogleSignIn.getLastSignedInAccount(context).getAccount());

        return new PeopleService.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(context.getString(R.string.app_name)).build();
    }

    public static Person getProfile() {
        try {
            return getService().people().get("people/me")
                    .setPersonFields("genders,birthdays,addresses")
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getBirthday(Person person) {
        try {
            List<Birthday> birthdayList = person.getBirthdays();
            if (birthdayList == null) return "";
            Date date = null;
            for (Birthday birthday : birthdayList) {
                date = birthday.getDate();
                if (date != null && date.size() >= 3) break;
                else date = null;
            }
            if (date == null) return "";
            Calendar calendar = Calendar.getInstance();
            calendar.set(date.getYear(), date.getMonth() - 1, date.getDay());
            DateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy hh:mm:ss");
            String strDate = dateFormat.format(date);
            String formatted = dateFormat.format(calendar.getTime());
            System.out.println(formatted);
            // Output "2012-09-26"

            System.out.println(dateFormat.parse(formatted));
            return formatted;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static final String CITY_SUFFIX = " city";
    public static android.location.Address getLocation(Person person) {
        try {
            List<Address> addressList = person.getAddresses();
            if (addressList == null) return null;
            String city = null;
            for (Address add : addressList) {
                city = add.getCity();
                if (!TextUtils.isEmpty(city)) break;
            }
            if (TextUtils.isEmpty(city)) return null;

            Geocoder geocoder = new Geocoder(App.getAppContext());

            List<android.location.Address> addresses =  geocoder.getFromLocationName(city + CITY_SUFFIX, 1);
            if (addresses == null || addresses.isEmpty()) return null;
            return addresses.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getGender(Person person) {
        List<Gender> genders = person.getGenders();
        if (genders == null || genders.isEmpty()) return null;
        Gender gender = genders.get(0);
        return String.valueOf(gender.getValue());
    }
}

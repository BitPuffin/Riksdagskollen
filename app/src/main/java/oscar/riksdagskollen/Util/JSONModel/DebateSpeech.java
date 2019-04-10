package oscar.riksdagskollen.Util.JSONModel;

import android.os.Parcel;
import android.os.Parcelable;

public class DebateSpeech implements Parcelable {

    private String parti;
    private String anf_nummer;
    private String anf_datumtid;
    private String intressent_id;
    private String talare;
    private String anf_klockslag;
    private String tumnagel;

    public String getTumnagel() {
        return tumnagel;
    }

    public String getParti() {
        return parti;
    }

    public String getAnf_nummer() {
        return anf_nummer;
    }

    public String getAnf_datumtid() {
        return anf_datumtid;
    }

    public String getIntressent_id() {
        return intressent_id;
    }

    public String getTalare() {
        return talare;
    }

    public String getAnf_klockslag() {
        return anf_klockslag;
    }

    public DebateSpeech() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.parti);
        dest.writeString(this.anf_nummer);
        dest.writeString(this.anf_datumtid);
        dest.writeString(this.intressent_id);
        dest.writeString(this.talare);
        dest.writeString(this.anf_klockslag);
        dest.writeString(this.tumnagel);
    }

    protected DebateSpeech(Parcel in) {
        this.parti = in.readString();
        this.anf_nummer = in.readString();
        this.anf_datumtid = in.readString();
        this.intressent_id = in.readString();
        this.talare = in.readString();
        this.anf_klockslag = in.readString();
        this.tumnagel = in.readString();
    }

    public static final Creator<DebateSpeech> CREATOR = new Creator<DebateSpeech>() {
        @Override
        public DebateSpeech createFromParcel(Parcel source) {
            return new DebateSpeech(source);
        }

        @Override
        public DebateSpeech[] newArray(int size) {
            return new DebateSpeech[size];
        }
    };
}
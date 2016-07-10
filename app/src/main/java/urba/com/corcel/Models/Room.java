package urba.com.corcel.Models;

/**
 * Created by aleja_000 on 09/07/2016.
 */
public class Room {
    private String Name;
    private String Id;
    private String Hash;
    private boolean noPass;

    public Room(String name, String id, String hash, boolean noPass) {
        Name = name;
        Id = id;
        Hash = hash;
        this.noPass = noPass;
    }

    public void setHash(String hash) {
        Hash = hash;
    }
    public String getName() {
        return Name;
    }
    public void setName(String Name) {
        this.Name = Name;
    }
    public String getId() {
        return Id;
    }
    public void setId(String Id) {
        this.Id = Id;
    }
    public boolean isNoPass() {
        return noPass;
    }
    public void setNoPass(boolean noPass) {
        this.noPass = noPass;
    }
    public String getHash() {
        return Hash;
    }

    @Override
    public String toString() {
        return Name;
    }
}
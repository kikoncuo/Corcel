package urba.com.corcel.Models;

/**
 * Created by aleja_000 on 09/07/2016.
 */
public class Room {
    private String Name;
    private String Id;
    private String Hash;

    public Room(String name, String id, String hash) {
        Name = name;
        Id = id;
        Hash = hash;
    }

    public String getHash() {

        return Hash;
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

    @Override
    public String toString() {
        return Name;
    }
}
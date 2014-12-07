# Easy DB
A simple and easy database system developed for Android.

### Installation

This repository is not included yet on the OSS, so you need to clone it and include as a module on `Android Studio`.

## Quick Usage

Currently this DB system uses the `SharedPreferences` mixed with `json` to store the data.

Up to this day it is only available simple collections with one-to-one and one-to-many relations.

**User.java**
```java
public class User extends AbstractHasManyBean {
    
    private String id = "";
    private String name = "";

    public User() {
        // current class, related class, cache opt
        super(User.class, Note.class, true);
    }

    // always use with context, otherwise you need to set it
    // manually with setContext method.
    public User(Context context) {
        // current class, related class, cache opt, context
        super(User.class, Note.class, true, context);
    }

    public static User instanceFromJson(JSONObject json, Context context) {
        User obj = new User(context);

        try {
            obj.setId(json.getString("id"));
            obj.setName(json.getString("name"));
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    public static User find(String id, Context context) {
        User instance = new User(context);

        return (User) instance.find(id);
    }

    @Override
    public JSONObject toJson() {
        JSONObject object = new JSONObject();

        try {
            object.put("id", id);
            object.put("name", name);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return object;
    }

    @Override
    public Namespace fromJson(JSONObject json) {
        return Namespace.instanceFromJson(json, context);
    }

    @Override
    public String getId() { return id;  }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

**Note.java**
```java
public class Note extends AbstractBean {
    
    private String id = "";
    private String title = "";
    private String content = "";

    public Note() {
        // current class, cache opt
        super(Note.class, true);
    }

    // always use with context, otherwise you need to set it
    // manually with setContext method.
    public Note(Context context) {
        // current class, cache opt, context
        super(Note.class, true, context);
    }

    public static Note instanceFromJson(JSONObject json, Context context) {
        Note obj = new Note(context);

        try {
            obj.setId(json.getString("id"));
            obj.setTitle(json.getString("title"));
            obj.setContent(json.getString("content"));
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    public static Note find(String id, Context context) {
        Note instance = new Note(context);

        return (Note) instance.find(id);
    }

    @Override
    public JSONObject toJson() {
        JSONObject object = new JSONObject();

        try {
            object.put("id", id);
            object.put("title", title);
            object.put("content", content);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return object;
    }

    @Override
    public Note fromJson(JSONObject json) {
        return Note.instanceFromJson(json, context);
    }

    @Override
    public String getId() { return id;  }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
```

Now you can test your application:

**ExampleActivity.java**
```java
public class ExampleActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        User user = new User(this);

        user.setId("1");
        user.setName("Tom");

        Note a = new Note(this);

        a.setId("1");
        a.setTitle("First title");
        a.setContent("First content");

        Note b = new Note(this);

        b.setId("2");
        b.setTitle("Second title");
        b.setContent("Second content");

        user.save();
        a.save();
        b.save();

        user.addChild(a);
        user.addChild(b);

        user = null;

        user = User.find("1", this);

        if(user != null) {
            List<Note> children = user.findAllChildren();

            Log.d("User", user.toJson().toString());

            for(int i=0; i<children.size(); i++) {
                Log.d("Note " + i, children.get(i).toJson().toString());
            }
        }
    }
}
```

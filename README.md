# Easy DB
A simple and easy database system developed for Android.

### Installation

Using maven you can include the repository `com.mauriciogiordano:easydb:0.1.3`.

In gradle using maven `compile "com.mauriciogiordano:easydb:0.1.3"`.

The JAR can be [found here](http://search.maven.org/remotecontent?filepath=com/mauriciogiordano/easydb/0.1.3/easydb-0.1.3-sources.jar)

## Quick Usage

Currently this DB system uses the `SharedPreferences` mixed with `json` to store the data.

Up to this day it is only available simple collections with one-to-one and one-to-many relations.

You need to use the annotation `@ModelField` for every database field.

**User.java**
```java
public class User extends HasManyModel<User, Note> {
    
    @ModelField
    private String id = "";
    @ModelField
    private String name = "";

    // required empty constructor!
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

    @Override
    public String getId() { return id;  }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public static User fromJson(JSONObject json, Context context) {
        User dummy = new User(context);

        return dummy.fromJson(json);
    }

    public static User find(String id, Context context) {
        User dummy = new User(context);

        return dummy.find(id);
    }
}
```

**Note.java**
```java
public class Note extends Model<Note> {
    
    @ModelField
    private String id = "";
    @ModelField
    private String title = "";
    @ModelField
    private String content = "";
    @ModelField
    private List<String> someList = new ArrayList<String>();

    // required empty constructor!
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

    @Override
    public String getId() { return id;  }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getSomeList() { return someList; }
    public void setSomeList(List<String> someList) { this.someList = someList; }

    public static Note fromJson(JSONObject json, Context context) {
        Note dummy = new Note(context);

        return dummy.fromJson(json);
    }

    public static Note find(String id, Context context) {
        Note dummy = new Note(context);

        return dummy.find(id);
    }
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

## API

### model.fromJson

Creates an object from a JSON representation. To access it statically, you need to create a dummy instance.

```java
User user = User.fromJson(new JSONObject({ "id" : "1", "name" : "example" }));
user.setContext(context); // required if doing any operation like #save, #remove, etc.
```

### model.toJson()

Returns a representation of the object in JSON format.

```java
System.out.println(user.toJson()); // outputs: { "id" : "1", "name" : "example" }
```

### model.find(String id)

Finds an object with id `id`. To access it statically, you need to create a dummy instance.

```java
User user = User.find("1");
user.setContext(context); // required if doing any operation like #save, #remove, etc.
```

### model.findAll()

Finds all object of that model. You need to create a dummy instance.

```java
List<User> userList = User.findAll();
```

### model.save()

Saves the object.

```java
user.save();
```

### model.remove()

Removes the object.

```java
user.remove();
```

### model.addOnUpdateListener(onUpdateListener)

Adds an onUpdateListener that will be triggered whenever the object is CREATED|UPDATED|REMOVED.

```java
user.addOnUpdateListener(new OnUpdateListener {
    @Override
    public void onUpdate(Model model, Status status) {
        ...
    }
});
```

## TODO

* Support **id** of any class that implements `Comparable`.
* Allow **context** as parameter for `model.find, model.findAll, etc`.

There are also available some listeners, such as `OnUpdateListener`

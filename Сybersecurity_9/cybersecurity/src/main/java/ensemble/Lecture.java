package ensemble;

import javafx.scene.control.Tab;

public class Lecture {
    private String title;
    private String url;
    private Tab tab;

    public Lecture() {
    }

    public Lecture(String title) {
        this.title = title;
    }

    public Lecture(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public Lecture(String title, DraggableTab tab) {
        this.title = title;
        this.tab = tab;
    }

    public Lecture(String title, String url, DraggableTab tab) {
        this.title = title;
        this.url = url;
        this.tab = tab;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    @Override
    public String toString() {
        return title;
    }
}

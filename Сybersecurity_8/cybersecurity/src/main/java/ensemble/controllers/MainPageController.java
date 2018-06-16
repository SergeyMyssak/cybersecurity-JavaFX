package ensemble.controllers;

import Service.AesCipherService;
import com.jfoenix.controls.JFXTreeView;
import ensemble.AlertHelper;
import ensemble.DraggingTabPaneSupport;
import ensemble.Lecture;
import ensemble.WebEventDispatcher;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static javafx.geometry.Orientation.HORIZONTAL;
import static javafx.geometry.Orientation.VERTICAL;

public class MainPageController {
    private static AesCipherService aes = new AesCipherService();
    private static AlertHelper alertHelper = new AlertHelper();

    private static Point2D pLimit;
    private static double width, height;

    /**
     * Путь для того, чтобы программа работала из среды разработки,
     * папка "Лекции", должна находиться на уровне проекта
     */
//    private final String dir = new File("").getAbsoluteFile().getParentFile().getAbsolutePath()
//            + System.getProperty("file.separator")
//            + "Лекции"
//            + System.getProperty("file.separator");

    /**
     * Путь для того, чтобы программа работала при открытии jar-файла
     */
    private final String dir = new File("").getAbsoluteFile()
            + System.getProperty("file.separator")
            + "Лекции"
            + System.getProperty("file.separator");

    @FXML
    private TabPane tabPane;
    @FXML
    private JFXTreeView<Lecture> mainTreeView;
    @FXML
    private MenuItem menuItemAboutUs;

    /**
     * Здесь находим всевозможные узлы, которые есть в нашей директории
     *
     * @param myFile - директория
     * @param root   - ветвь, в которую помещается объект Lecture
     */
    private static void findDirectoriesWithName(File myFile, TreeItem<Lecture> root) {
        for (File file : Objects.requireNonNull(myFile.listFiles())) {

            // если это директория, т.е. узел, то идём дальше
            if (file.isDirectory()) {

                // в 2018 Microsoft Word конвентирование Word-файла происходит
                // с появлением папки .files, в которой лежат все ресурсы нужные для
                // корректной работы html-файла.
                //
                // Так как папка .files является директорией, то в нашем дереве,
                // она обязательно отобразится, поэтому мы игнорируем все папки,
                // с таким названием, чтобы избежать её появления в нашем дереве
                if (!file.getName().endsWith(".files")) {
                    TreeItem<Lecture> node = new TreeItem<>(new Lecture(file.getName(), file.getPath()));
                    root.getChildren().add(node);

                    // доходим до конечной папки, попутно добавляя их в node
                    findDirectoriesWithName(file, node);

                    node.setExpanded(true);
                }
            }
        }
    }

    @FXML
    void initialize() {
        loadListView();
    }

    /**
     * Заполняем JFXTreeView<Lecture> объектами Lecture,
     * где, lecture.getTitle() - название узла
     */
    private void loadListView() {
        DraggingTabPaneSupport draggingTabPaneSupport = new DraggingTabPaneSupport();
        draggingTabPaneSupport.addSupport(tabPane);

        // корень нашего дерева
        TreeItem<Lecture> root = new TreeItem<>(new Lecture("Лекторий", dir));
        root.setExpanded(true);

        File file = new File(dir);

        try {
            findDirectoriesWithName(file, root);
        } catch (Exception ex) {
            alertHelper.configureAlert(Alert.AlertType.ERROR,
                    "Ошибка",
                    null,
                    "Не найдена папка \"Лекции\". " +
                            "Дальнейшая работа программы невозможна. " +
                            "Программа будет аварийно завершена.");

            System.exit(0);
        }

        mainTreeView.setRoot(root);

        mainTreeView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    Lecture lecture = newValue.getValue();

                    // если выбранный элемент не имеет детей и не является директорией
                    // выбранный элемент должен быть файлом
                    if (newValue.isLeaf()) {
                        switchTab(lecture);
                    }
                });
    }

    /**
     * Происходит переключение между закладками, либо их добавление на панель закладок
     *
     * @param selectedItem - объект, полученный при нажатии на TreeItem<Lecture>
     */
    private void switchTab(Lecture selectedItem) {
        if (!tabPane.getTabs().contains(selectedItem.getTab())) {
            addTab(selectedItem);
        } else {
            tabPane.getSelectionModel().select(selectedItem.getTab());
        }
    }

    /**
     * Добавляем закладку на панель закладок, предварительно загрузив в неё html-файл
     *
     * @param selectedItem - объект, полученный при нажатии на TreeItem<Lecture>
     */
    private void addTab(Lecture selectedItem) {
        try {
            Node node = FXMLLoader.load(getClass().getResource("/ensemble/fxmls/Space.fxml"));
            WebView webView = (WebView) node;
            WebEngine webEngine = webView.getEngine();

            String url = getUrl(selectedItem.getTitle(), selectedItem.getUrl());

            // получаем код/контент из html-файла
            String htmlCode = AesCipherService.initAesCipher(url);

            if (htmlCode != null) {
                String urlImg = getUrlImg(selectedItem.getUrl());

                Document document = Jsoup.parse(htmlCode);
                Elements tagsImg = document.getElementsByTag("img");

                // в каждом атрибуте src тега img вставляем абсолютную ссылку
                tagsImg.forEach(tagImg -> {
                    String a = tagImg.attr("src");
                    tagImg.attr("src", urlImg + a);
                });

                // запрещаем копирование информации
                WebEventDispatcher webEventDispatcher = new WebEventDispatcher(webView.getEventDispatcher());
                webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.equals(Worker.State.SUCCEEDED)) {
                        // dispatch all events
                        webView.setEventDispatcher(webEventDispatcher);
                    }
                });

                webView.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {

                    @Override
                    public void onChanged(Change<? extends Node> c) {
                        pLimit = webView.localToScene(webView.getWidth(), webView.getHeight());
                        webView.lookupAll(".scroll-bar").stream()
                                .map(s -> (ScrollBar) s).forEach(s -> {
                            if (s.getOrientation().equals(VERTICAL)) {
                                width = s.getBoundsInLocal().getWidth();
                            }
                            if (s.getOrientation().equals(HORIZONTAL)) {
                                height = s.getBoundsInLocal().getHeight();
                            }
                        });
                        // dispatch all events
                        webEventDispatcher.setLimit(pLimit.subtract(width, height));
                    }
                });

                webEngine.loadContent(String.valueOf(document));

                Tab tab = new Tab(selectedItem.getTitle(), node);
                tabPane.getTabs().add(tab);

                selectedItem.setTab(tab);
                tabPane.getSelectionModel().select(tab);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Получаем путь к html-файлу
     *
     * @param fileName - имя html-файла
     * @param filePath - путь до директории, в которой находится файл
     * @return полный url файла
     */
    private String getUrl(String fileName, String filePath) {
        return filePath
                + System.getProperty("file.separator")
                + fileName.substring(4)
                + ".html";
    }

    /**
     * Получаем путь к фотографиям
     *
     * @param filePath - путь до директории, в которой находится файл
     * @return url до директории, в которой находится файл
     */
    private String getUrlImg(String filePath) {
        return "file:"
                + System.getProperty("file.separator")
                + filePath
                + System.getProperty("file.separator");
    }

    /**
     * Открываем модальное окно при нажатии на элемент "About us" из меню "Help"
     *
     * @throws IOException - файл модального окна не найден
     */

    @FXML
    private void onActionMenuItemAboutUs() throws IOException {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/ensemble/fxmls/AboutUs.fxml"));

        stage.setScene(new Scene(root));
        stage.setTitle("About us");

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getIcons().add(new Image("/ensemble/images/icon.png"));

        stage.setResizable(false);
        stage.show();
    }
}

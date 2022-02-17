module escom.practica1 {
    requires javafx.controls;
    requires javafx.fxml;

    opens escom.practica1 to javafx.fxml;
    exports escom.practica1;
}

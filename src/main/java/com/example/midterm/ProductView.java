package com.example.midterm;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Route("")
public class ProductView extends VerticalLayout {
    private ComboBox<Product> productBoxLists;
    private TextField productNameField;
    private NumberField productCostField, productProfitField, productPriceField;
    private HorizontalLayout buttonLayout;
    private Button addProductBtn, updateProductBtn, deleteProductBtn, clearProductBtn;

    public ProductView() {
        productBoxLists = new ComboBox<Product>("Product List");
        productNameField = new TextField("Product Name:");
        productCostField = new NumberField("Product Cost:");
        productProfitField = new NumberField("Product Profit:");
        productPriceField = new NumberField("Product Price:");
        buttonLayout = new HorizontalLayout();
        addProductBtn = new Button("Add Product");
        updateProductBtn = new Button("Update Product");
        deleteProductBtn = new Button("Delete Product");
        clearProductBtn = new Button("Clear Product");

        productBoxLists.getStyle().set("width", "600px");
        productNameField.getStyle().set("width", "600px");
        productCostField.getStyle().set("width", "600px");
        productProfitField.getStyle().set("width", "600px");
        productPriceField.getStyle().set("width", "600px");

        productPriceField.setEnabled(false);

        this.clearProduct();

        productBoxLists.addFocusListener(e -> {
            this.fetchProductLists();
        });
        productBoxLists.addValueChangeListener(e -> {
            if (this.productBoxLists.getValue() != null) {
                this.productNameField.setValue(this.productBoxLists.getValue().getProductName());
                this.productCostField.setValue(this.productBoxLists.getValue().getProductCost());
                this.productProfitField.setValue(this.productBoxLists.getValue().getProductProfit());
                this.productPriceField.setValue(this.productBoxLists.getValue().getProductPrice());
            } else {
                this.clearProduct();
            }
        });
        productCostField.addKeyPressListener(e -> {
            if (e.getKey().equals("Enter")) {
                this.calculatePrice();
            }
        });
        productProfitField.addKeyPressListener(e -> {
            if (e.getKey().equals("Enter")) {
                this.calculatePrice();
            }
        });

        addProductBtn.addClickListener(e -> {
            this.calculatePrice();
            String name = this.productNameField.getValue();
            double cost = this.productCostField.getValue();
            double profit = this.productProfitField.getValue();
            double price = this.productPriceField.getValue();

            WebClient
                .create()
                .post()
                .uri("http://localhost:8080/addProduct")
                .body(Mono.just(new Product(null, name, cost, profit, price)), Product.class)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

            this.fetchProductLists();
        });
        updateProductBtn.addClickListener(e -> {
            this.calculatePrice();
            String id = this.productBoxLists.getValue().get_id();
            String name = this.productNameField.getValue();
            double cost = this.productCostField.getValue();
            double profit = this.productProfitField.getValue();
            double price = this.productPriceField.getValue();

            WebClient
                .create()
                .post()
                .uri("http://localhost:8080/updateProduct")
                .body(Mono.just(new Product(id, name, cost, profit, price)), Product.class)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

            this.fetchProductLists();
        });
        deleteProductBtn.addClickListener(e -> {
            String id = this.productBoxLists.getValue().get_id();
            String name = this.productNameField.getValue();
            double cost = this.productCostField.getValue();
            double profit = this.productProfitField.getValue();
            double price = this.productPriceField.getValue();

            WebClient
                .create()
                .post()
                .uri("http://localhost:8080/deleteProduct")
                .body(Mono.just(new Product(id, name, cost, profit, price)), Product.class)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

            this.fetchProductLists();
            this.clearProduct();
        });
        clearProductBtn.addClickListener(e -> {
            this.clearProduct();
            new Notification("Cleared", 500).open();
        });

        buttonLayout.add(addProductBtn, updateProductBtn, deleteProductBtn, clearProductBtn);
        this.add(productBoxLists, productNameField, productCostField, productProfitField, productPriceField, buttonLayout);
    }

    private void fetchProductLists() {
        List<Product> p = WebClient
                        .create()
                        .get()
                        .uri("http://localhost:8080/getProducts")
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<Product>>() {})
                        .block();

        this.productBoxLists.setItems(p);
    }

    private ComponentEventListener clearProduct() {
        productNameField.setValue("");
        productCostField.setValue(0.0);
        productProfitField.setValue(0.0);
        productPriceField.setValue(0.0);
        this.fetchProductLists();
        return null;
    }

    private void calculatePrice() {
        double cost = this.productCostField.getValue();
        double profit = this.productProfitField.getValue();

        Double price = WebClient
                        .create()
                        .get()
                        .uri("http://localhost:8080/getPrice/" + cost + "/" + profit)
                        .retrieve()
                        .bodyToMono(Double.class)
                        .block();
        this.productPriceField.setValue(price);
    }
}

package org.agoncal.fascicle.quarkus.book;

//import io.quarkus.test.junit.DisabledOnNativeImage;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.agoncal.fascicle.quarkus.book.Book;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
// end::adocImports[]

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

// tag::adocImportStatic[]
import static io.restassured.RestAssured.given;
//import javax.ws.rs.core.HttpHeaders;
//import javax.ws.rs.core.MediaType;

import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
//import static javax.ws.rs.core.HttpHeaders.ACCEPT;
//import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
//import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
//import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
//import static javax.ws.rs.core.Response.Status.CREATED;
//import static javax.ws.rs.core.Response.Status.NOT_FOUND;
//import static javax.ws.rs.core.Response.Status.NO_CONTENT;
//import static javax.ws.rs.core.Response.Status.OK;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
// end::adocImpor

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookResourceTest {

  private static final String DEFAULT_TITLE = "Title";
  private static final String UPDATED_TITLE = "Title (updated)";
  private static final String DEFAULT_AUTHOR = "Author";
  private static final String UPDATED_AUTHOR = "Author (updated)";
  private static final Integer DEFAULT_YEAR_OF_PUBLICATION = 1111;
  private static final Integer UPDATED_YEAR_OF_PUBLICATION = 2222;
  private static final Integer DEFAULT_NB_OF_PAGES = 111;
  private static final Integer UPDATED_NB_OF_PAGES = 222;
  private static final Integer DEFAULT_RANK = 1;
  private static final Integer UPDATED_RANK = 2;
  private static final BigDecimal DEFAULT_PRICE = new BigDecimal(11.0);
  private static final BigDecimal UPDATED_PRICE = new BigDecimal(22.0);
  private static final URL DEFAULT_SMALL_IMAGE_URL = makeUrl("http://www.url.com");
  private static final URL UPDATED_SMALL_IMAGE_URL = makeUrl("http://www.updatedurl.com");
  private static final URL DEFAULT_MEDIUM_IMAGE_URL = makeUrl("http://www.url.com");
  private static final URL UPDATED_MEDIUM_IMAGE_URL = makeUrl("http://www.updatedurl.com");
  private static final String DEFAULT_DESCRIPTION = "Description";
  private static final String UPDATED_DESCRIPTION = "Description (updated)";

  private static URL makeUrl(String urlString) {
    try {
      return new URL(urlString);
    } catch (MalformedURLException e) {
      return null;
    }
  }

  private static int nbBooks;
  private static String bookId;

  @Test
  @Order(1)
  void shouldGetInitialBooks() {
    List<Book> books =
      given()
        .header(ACCEPT, APPLICATION_JSON).
        when()
        .get("/api/books").
        then()
        .statusCode(OK.getStatusCode())
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .extract().body().as(getBookTypeRef());

    nbBooks = books.size();
  }

  private TypeRef<List<Book>> getBookTypeRef() {
    return new TypeRef<List<Book>>() {
    };
  }


  @Test
  @Order(2)
  void shouldAddABook() {
    Book book = new Book();
    book.title = DEFAULT_TITLE;
    book.author = DEFAULT_AUTHOR;
    book.yearOfPublication = DEFAULT_YEAR_OF_PUBLICATION;
    book.nbOfPages = DEFAULT_NB_OF_PAGES;
    book.rank = DEFAULT_RANK;
    book.price = DEFAULT_PRICE;
    book.smallImageUrl = DEFAULT_SMALL_IMAGE_URL;
    book.mediumImageUrl = DEFAULT_MEDIUM_IMAGE_URL;
    book.description = DEFAULT_DESCRIPTION;

    // Persists a new book
    String location =
      given()
        .body(book)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(ACCEPT, APPLICATION_JSON).
        when()
        .post("/api/books").
        then()
        .statusCode(CREATED.getStatusCode())
        .extract().header("Location");

    // Extracts the Location and stores the book id
    assertTrue(location.contains("/api/books"));
    String[] segments = location.split("/");
    bookId = segments[segments.length - 1];
    assertNotNull(bookId);

    // Checks the book has been created
    given()
      .header(ACCEPT, APPLICATION_JSON)
      .pathParam("id", bookId).
      when()
      .get("/api/books/{id}").
      then()
      .statusCode(OK.getStatusCode())
      .header(CONTENT_TYPE, APPLICATION_JSON)
      .body("title", Is.is(DEFAULT_TITLE))
      .body("author", Is.is(DEFAULT_AUTHOR))
      .body("yearOfPublication", Is.is(DEFAULT_YEAR_OF_PUBLICATION))
      .body("nbOfPages", Is.is(DEFAULT_NB_OF_PAGES))
      .body("rank", Is.is(DEFAULT_RANK))
      .body("smallImageUrl", Is.is(DEFAULT_SMALL_IMAGE_URL.toString()))
      .body("mediumImageUrl", Is.is(DEFAULT_MEDIUM_IMAGE_URL.toString()))
      .body("description", Is.is(DEFAULT_DESCRIPTION));

    // Checks there is an extra book in the database
    List<Book> books =
      given().
        header(ACCEPT, APPLICATION_JSON).
        when()
        .get("/api/books").
        then()
        .statusCode(OK.getStatusCode())
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .extract().body().as(getBookTypeRef());

    assertEquals(nbBooks + 1, books.size());
  }


  @Test
  @Order(3)
  void shouldUpdateABook() {
    Book book = new Book();
    book.id = Long.valueOf(bookId);
    book.title = UPDATED_TITLE;
    book.author = UPDATED_AUTHOR;
    book.yearOfPublication = UPDATED_YEAR_OF_PUBLICATION;
    book.nbOfPages = UPDATED_NB_OF_PAGES;
    book.rank = UPDATED_RANK;
    book.price = UPDATED_PRICE;
    book.smallImageUrl = UPDATED_SMALL_IMAGE_URL;
    book.mediumImageUrl = UPDATED_MEDIUM_IMAGE_URL;
    book.description = UPDATED_DESCRIPTION;

    // Updates the previously created book
    given()
      .body(book)
      .header(CONTENT_TYPE, APPLICATION_JSON)
      .header(ACCEPT, APPLICATION_JSON).
      when()
      .put("/api/books").
      then()
      .statusCode(OK.getStatusCode())
      .header(CONTENT_TYPE, APPLICATION_JSON)
      .body("title", Is.is(UPDATED_TITLE))
      .body("author", Is.is(UPDATED_AUTHOR))
      .body("yearOfPublication", Is.is(UPDATED_YEAR_OF_PUBLICATION))
      .body("nbOfPages", Is.is(UPDATED_NB_OF_PAGES))
      .body("rank", Is.is(UPDATED_RANK))
      .body("price", Is.is(UPDATED_PRICE.intValue()))
      .body("smallImageUrl", Is.is(UPDATED_SMALL_IMAGE_URL.toString()))
      .body("mediumImageUrl", Is.is(UPDATED_MEDIUM_IMAGE_URL.toString()))
      .body("description", Is.is(UPDATED_DESCRIPTION));
  }


  @Test
  @Order(4)
  void shouldRemoveABook() {
    // Deletes the previously created book
    given()
      .pathParam("id", bookId).
      when()
      .delete("/api/books/{id}").
      then()
      .statusCode(NO_CONTENT.getStatusCode());

    // Checks there is less a book in the database
    List<Book> books =
      given()
        .header(ACCEPT, APPLICATION_JSON).
        when()
        .get("/api/books").
        then()
        .statusCode(OK.getStatusCode())
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .extract().body().as(getBookTypeRef());

    assertEquals(nbBooks, books.size());
  }
// end::adocShouldRemoveAnItem[]

  @Test
  void shouldPingLiveness() {
    given().
      when()
      .get("/health/live").
      then()
      .statusCode(OK.getStatusCode());
  }

  @Test
  void shouldPingReadiness() {
    given().
      when()
      .get("/health/ready").
      then()
      .statusCode(OK.getStatusCode());
  }

  @Test
  void shouldPingMetrics() {
    given()
      .header(ACCEPT, APPLICATION_JSON).
      when()
      .get("/metrics/application").
      then()
      .statusCode(OK.getStatusCode());
  }


  @Test
  void shouldPingSwaggerUI() {
    given().
      when()
      .get("/swagger-ui").
      then()
      .statusCode(OK.getStatusCode());
  }
  // end::adocOpenAPI[]

}

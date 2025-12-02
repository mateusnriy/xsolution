package xsolution.utils;

public class FilterHelper {
  public static boolean matchString(String itemValue, String filterValue) {
    if (filterValue == null || filterValue.trim().isEmpty()) {
      return true;
    }
    if (itemValue == null) {
      return false;
    }
    return itemValue.toLowerCase().contains(filterValue.toLowerCase().trim());
  }

  public static boolean matchEquals(Object itemValue, Object filterValue) {
    if (filterValue == null) {
      return true;
    }
    if (itemValue == null) {
      return false;
    }
    return itemValue.equals(filterValue);
  }
}
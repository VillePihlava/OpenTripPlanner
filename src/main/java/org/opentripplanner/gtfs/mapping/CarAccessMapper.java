package org.opentripplanner.gtfs.mapping;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.opentripplanner.transit.model.network.CarAccess;

/**
 * This class is similar to BikeAccessMapper but for car access instead of bike access.
 * The semantics for car access might be different and are not defined here.
 * This is a placeholder for how car access could be managed within a similar framework.
 * TODO comment about this mapper
 */
class CarAccessMapper {

  public static CarAccess mapForTrip(Trip rhs) {
    return mapValues(rhs.getCarsAllowed());
  }

  public static CarAccess mapForRoute(Route rhs) {
    return mapValues(rhs.getCarsAllowed());
  }

  private static CarAccess mapValues(int carsAllowed) {
    switch (carsAllowed) {
      case 1:
        return CarAccess.ALLOWED;
      case 2:
        return CarAccess.NOT_ALLOWED;
      default:
        return CarAccess.UNKNOWN;
    }
  }
}

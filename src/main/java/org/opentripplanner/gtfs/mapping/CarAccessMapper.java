package org.opentripplanner.gtfs.mapping;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.opentripplanner.transit.model.network.CarAccess;

/**
 * TODO comment about this mapper
 */
class CarAccessMapper {

  public static CarAccess mapForTrip(Trip rhs) {
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

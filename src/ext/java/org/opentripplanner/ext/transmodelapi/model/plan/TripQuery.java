package org.opentripplanner.ext.transmodelapi.model.plan;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLOutputType;
import org.opentripplanner.ext.transmodelapi.TransmodelGraphQLPlanner;
import org.opentripplanner.ext.transmodelapi.model.DefaultRoutingRequestType;
import org.opentripplanner.ext.transmodelapi.model.EnumTypes;
import org.opentripplanner.ext.transmodelapi.model.TransportModeSlack;
import org.opentripplanner.ext.transmodelapi.model.framework.LocationInputType;
import org.opentripplanner.ext.transmodelapi.support.GqlUtil;
import org.opentripplanner.routing.api.request.RequestFunctions;

public class TripQuery {


  public static GraphQLFieldDefinition create(
      DefaultRoutingRequestType routing,
      GraphQLOutputType tripType,
      GqlUtil gqlUtil
  ) {
    return GraphQLFieldDefinition.newFieldDefinition()
        .name("trip")
        .description("Input type for executing a travel search for a trip between two locations. Returns trip patterns describing suggested alternatives for the trip.")
        .type(tripType)
        .argument(GraphQLArgument.newArgument()
            .name("dateTime")
            .description("Date and time for the earliest time the user is willing to start the journey (if arriveBy=false/not set) or the latest acceptable time of arriving (arriveBy=true). Defaults to now")
            .type(gqlUtil.dateTimeScalar)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("searchWindow")
            .description("The length of the search-window in minutes. This is normally dynamically calculated by the server, but you may override this by setting it. The search-window used in a request is returned in the response metadata. To get the \"next page\" of trips use the metadata(searchWindowUsed and nextWindowDateTime) to create a new request. If not provided the value is resolved depending on the other input parameters, available transit options and realtime changes.")
            .type(Scalars.GraphQLInt)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("from")
            .description("The start location")
            .type(new GraphQLNonNull(LocationInputType.INPUT_TYPE))
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("to")
            .description("The end location")
            .type(new GraphQLNonNull(LocationInputType.INPUT_TYPE))
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("wheelchairAccessible")
            .description("Whether the trip must be wheelchair accessible. NOT IMPLEMENTED")
            .type(Scalars.GraphQLBoolean)
            .defaultValue(routing.request.wheelchairAccessible)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("numTripPatterns")
            .description("The maximum number of trip patterns to return for this search "
                + "window. This may cause relevant trips to be missed, so it is preferable "
                + "to set this high and use other filtering options instead.")
            .defaultValue(routing.request.numItineraries)
            .type(Scalars.GraphQLInt)
            .build())
        //                .argument(GraphQLArgument.newArgument()
        //                        .name("maximumWalkDistance")
        //                        .description("DEPRECATED - Use maxPreTransitWalkDistance/maxTransferWalkDistance instead. " +
        //                                "The maximum distance (in meters) the user is willing to walk. Note that trip patterns with " +
        //                                "longer walking distances will be penalized, but not altogether disallowed. Maximum allowed value is 15000 m")
        //                        .defaultValue(routing.request.maxWalkDistance)
        //                        .type(Scalars.GraphQLFloat)
        //                        .build())
        //                .argument(GraphQLArgument.newArgument()
        //                        .name("maxTransferWalkDistance")
        //                        .description("The maximum walk distance allowed for transfers.")
        //                        .defaultValue(routing.request.maxTransferWalkDistance)
        //                        .type(Scalars.GraphQLFloat)
        //                        .build())
        .argument(GraphQLArgument.newArgument()
            .name("walkSpeed")
            .description("The maximum walk speed along streets, in meters per second")
            .type(Scalars.GraphQLFloat)
            .defaultValue(routing.request.walkSpeed)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("bikeSpeed")
            .description("The maximum bike speed along streets, in meters per second")
            .type(Scalars.GraphQLFloat)
            .defaultValue(routing.request.bikeSpeed)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("bicycleOptimisationMethod")
            .description("The set of characteristics that the user wants to optimise for during bicycle searches -- defaults to "
                + enumValAsString(EnumTypes.BICYCLE_OPTIMISATION_METHOD, routing.request.optimize))
            .type(EnumTypes.BICYCLE_OPTIMISATION_METHOD)
            .defaultValue(routing.request.optimize)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("arriveBy")
            .description("Whether the trip should depart at dateTime (false, the default), or arrive at dateTime.")
            .type(Scalars.GraphQLBoolean)
            .defaultValue(routing.request.arriveBy)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("banned")
            .description("Banned")
            .description("Parameters for indicating authorities, lines or quays not be used in the trip patterns")
            .type(BannedInputType.INPUT_TYPE)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("whiteListed")
            .description("Whitelisted")
            .description("Parameters for indicating the only authorities, lines or quays to be used in the trip patterns")
            .type(JourneyWhiteListed.INPUT_TYPE)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("transferPenalty")
            .description("An extra penalty added on transfers (i.e. all boardings except the first one). The transferPenalty is used when a user requests even less transfers. In the latter case, we don't actually optimise for fewest transfers, as this can lead to absurd results. Consider a trip in New York from Grand Army Plaza (the one in Brooklyn) to Kalustyan's at noon. The true lowest transfers trip pattern is to wait until midnight, when the 4 train runs local the whole way. The actual fastest trip pattern is the 2/3 to the 4/5 at Nevins to the 6 at Union Square, which takes half an hour. Even someone optimise for fewest transfers doesn't want to wait until midnight. Maybe they would be willing to walk to 7th Ave and take the Q to Union Square, then transfer to the 6. If this takes less than transferPenalty seconds, then that's what we'll return.")
            .type(Scalars.GraphQLInt)
            .defaultValue(routing.request.transferCost)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("modes")
            .description("The set of access/egress/direct/transit modes to be used for "
                + "this search.")
            .type(ModeInputType.INPUT_TYPE)
            .build())
        //                .argument(GraphQLArgument.newArgument()
        //                         .name("transportSubmodes")
        //                         .description("Optional set of allowed submodes per transport mode provided in 'modes'. If at least one submode is set for a transport mode all submodes not set will be disregarded. Note that transportMode must also be included in 'modes' for the submodes to be allowed")
        //                        .type(new GraphQLList(transportSubmodeFilterInputType))
        //                         .defaultValue(List.of())
        //                         .build())
        //                .argument(GraphQLArgument.newArgument()
        //                        .name("minimumTransferTime")
        //                        .description("DEPRECATED - Use 'transferSlack/boardSlack/alightSlack' instead.  ")
        //                        .type(Scalars.GraphQLInt)
        //                        .defaultValue(routing.request.transferSlack)
        //                        .build())
        .argument(GraphQLArgument.newArgument()
            .name("transferSlack")
            .description("An expected transfer time (in seconds) that specifies the amount of time that must pass between exiting one public transport vehicle and boarding another. This time is in addition to time it might take to walk between stops.")
            .type(Scalars.GraphQLInt)
            .defaultValue(routing.request.transferSlack)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("boardSlackDefault")
            .description(TransportModeSlack.boardSlackDescription("boardSlackList"))
            .type(Scalars.GraphQLInt)
            .defaultValue(routing.request.boardSlack)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("boardSlackList")
            .description(TransportModeSlack.slackByGroupDescription(
                "boardSlack", routing.request.boardSlackForMode
            ))
            .type(TransportModeSlack.SLACK_LIST_INPUT_TYPE)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("alightSlackDefault")
            .description(TransportModeSlack.alightSlackDescription("alightSlackList"))
            .type(Scalars.GraphQLInt)
            .defaultValue(routing.request.alightSlack)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("alightSlackList")
            .description(TransportModeSlack.slackByGroupDescription(
                "alightSlack", routing.request.alightSlackForMode
            ))
            .type(TransportModeSlack.SLACK_LIST_INPUT_TYPE)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("maximumTransfers")
            .description("Maximum number of transfers")
            .type(Scalars.GraphQLInt)
            .defaultValue(routing.request.maxTransfers)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("ignoreRealtimeUpdates")
            .description("When true, realtime updates are ignored during this search.")
            .type(Scalars.GraphQLBoolean)
            .defaultValue(routing.request.ignoreRealtimeUpdates)
            .build())
        //                .argument(GraphQLArgument.newArgument()
        //                        .name("includePlannedCancellations")
        //                        .description("When true, service journeys cancelled in scheduled route data will be included during this search.")
        //                        .type(Scalars.GraphQLBoolean)
        //                        .defaultValue(defaultRoutingRequest.defaults.includePlannedCancellations)
        //                        .build())
        //                .argument(GraphQLArgument.newArgument()
        //                        .name("ignoreInterchanges")
        //                        .description("DEPRECATED - For debugging only. Ignores interchanges defined in timetable data.")
        //                        .type(Scalars.GraphQLBoolean)
        //                        .defaultValue(defaultRoutingRequest.defaults.ignoreInterchanges)
        //                        .build())
        .argument(GraphQLArgument.newArgument()
            .name("locale")
            .type(EnumTypes.LOCALE)
            .defaultValue("no")
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("transitGeneralizedCostLimit")
            .description("Set a relative limit for all transit itineraries. The limit "
                + "is calculated based on the best transit itinerary generalized-cost. "
                + "Itineraries without transit legs are excluded from this filter. "
                + "Example: f(x) = 3600 + 2.0 x. If the lowest cost returned is 10 000, "
                + "then the limit is set to: 3 600 + 2 * 10 000 = 26 600. Then all "
                + "itineraries with at least one transit leg and a cost above 26 600 "
                + "is removed from the result. Default: "
                + RequestFunctions.serialize(routing.request.transitGeneralizedCostLimit)
            )
            .type(gqlUtil.doubleFunctionScalar)
            // There is a bug in the GraphQL lib. The default value is shown as a
            // `boolean` with value `false`, not the actual value. Hence; The default
            // is added                    to the description above instead.
            // .defaultValue(routing.request.transitGeneralizedCostLimit)
            .build())
        //                .argument(GraphQLArgument.newArgument()
        //                        .name("compactLegsByReversedSearch")
        //                        .description("DEPRECATED - NO EFFECT IN OTP2")
        //                        .type(Scalars.GraphQLBoolean)
        //                        .defaultValue(false)
        //                        .build())
        //                .argument(GraphQLArgument.newArgument()
        //                        .name("reverseOptimizeOnTheFly")
        //                        .description("DEPRECATED - NO EFFECT IN OTP2.")
        //                        .type(Scalars.GraphQLBoolean)
        //                        .defaultValue(false)
        //                        .build())
        //                .argument(GraphQLArgument.newArgument()
        //                        .name("maxPreTransitTime")
        //                        .description("Maximum time for the ride part of \"kiss and ride\" and \"ride and kiss\".")
        //                        .type(Scalars.GraphQLInt)
        //                        .defaultValue(routing.request.maxPreTransitTime)
        //                        .build())
        //                .argument(GraphQLArgument.newArgument()
        //                        .name("preTransitReluctance")
        //                        .description("How much worse driving before and after transit is than riding on transit. Applies to ride and kiss, kiss and ride and park and ride.")
        //                        .type(Scalars.GraphQLFloat)
        //                        .defaultValue(defaultRoutingRequest.defaults.preTransitReluctance)
        //                        .build())
        //                .argument(GraphQLArgument.newArgument()
        //                        .name("maxPreTransitWalkDistance")
        //                        .description("Max walk distance for access/egress legs. NOT IMPLEMENTED")
        //                        .type(Scalars.GraphQLFloat)
        //                        .build())
        //                .argument(GraphQLArgument.newArgument()
        //                        .name("useFlex")
        //                        .type(Scalars.GraphQLBoolean)
        //                        .description("NOT IMPLEMENTED")
        //                        .build())
        //                .argument(GraphQLArgument.newArgument()
        //                        .name("banFirstServiceJourneysFromReuseNo")
        //                        .description("How many service journeys used in a tripPatterns should be banned from inclusion in successive tripPatterns. Counting from start of tripPattern.")
        //                        .type(Scalars.GraphQLInt)
        //                        .defaultValue(defaultRoutingRequest.defaults.banFirstTripsFromReuseNo)
        //                        .build())
        .argument(GraphQLArgument.newArgument()
            .name("walkReluctance")
            .description("Walk cost is multiplied by this value. This is the main parameter to use for limiting walking.")
            .type(Scalars.GraphQLFloat)
            .defaultValue(routing.request.walkReluctance)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("waitReluctance")
            .description("Wait cost is multiplied by this value. Setting this to a value lower than 1 indicates that waiting is better than staying on a vehicle. This should never be set higher than walkReluctance, since that would lead to walking down a line to avoid waiting.")
            .type(Scalars.GraphQLFloat)
            .defaultValue(routing.request.waitReluctance)
            .build())
        //                .argument(GraphQLArgument.newArgument()
        //                        .name("ignoreMinimumBookingPeriod")
        //                        .description("Ignore the MinimumBookingPeriod defined on the ServiceJourney and allow itineraries to start immediately after the current time.")
        //                        .type(Scalars.GraphQLBoolean)
        //                        .defaultValue(defaultRoutingRequest.defaults.ignoreDrtAdvanceBookMin)
        //                        .build())
        //                .argument(GraphQLArgument.newArgument()
        //                        .name("transitDistanceReluctance")
        //                        .description("The extra cost per meter that is travelled by transit. This is a cost point peter meter, so it should in most\n" +
        //                                "cases be a very small fraction. The purpose of assigning a cost to distance is often because it correlates with\n" +
        //                                "fare prices and you want to avoid situations where you take detours or travel back again even if it is\n" +
        //                                "technically faster. Setting this value to 0 turns off the feature altogether.")
        //                        .type(Scalars.GraphQLFloat)
        //                        .defaultValue(defaultRoutingRequest.defaults.transitDistanceReluctance)
        //                        .build())
        .argument(GraphQLArgument.newArgument()
            .name("debugItineraryFilter")
            .description("Debug the itinerary-filter-chain. The filters will mark itineraries as deleted, but NOT delete them when this is enabled.")
            .type(Scalars.GraphQLBoolean)
            .defaultValue(routing.request.debugItineraryFilter)
            .build())

        .dataFetcher(environment -> new TransmodelGraphQLPlanner().plan(environment))
        .build();
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private static String enumValAsString(GraphQLEnumType enumType, Enum<?> otpVal) {
    return enumType
        .getValues()
        .stream()
        .filter(e -> e.getValue().equals(otpVal))
        .findFirst()
        .get()
        .getName();
  }

}

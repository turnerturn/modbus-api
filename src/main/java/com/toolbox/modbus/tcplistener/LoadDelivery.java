package com.toolbox.modbus.tcplistener;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class LoadDelivery {
    public void process(Load load) throws Exception {
        assertDriverHasPhysicalAccess(load);
        decorateAvailableMeterAssignments(load);
        assertAtleastOneCompatibleSpot(load);
        queueLoadDelivery();

    }

    private void whenQueuedLoadIsNotCompatible() {
        // prompt user with alert and cancel load.
        cancelLoad();
    }

    private void whenQueuedLoadExpires() {
        // prompt user with alert and cancel load.
        cancelLoad();
    }

    private void whenLoadIsDelivered() {
        processLoadFulfillment();
    }

    private void whenPhysicalAccessGranted() {
        // find queued load.
        // if found, upload to meter.
        var queuedLoads = getQueuedLoadsByDriver();
        deliverQueuedLoads(queuedLoads);
        // else,deliver manual load.

        // prompt when driver can proceed with load. or alert on errors.
    }

    private List<Load> getQueuedLoadsByDriver() {
        return new ArrayList<Load>();
    }

    private void queueLoadDelivery() {

    } 

    private void deliverQueuedLoads(List<Load> queuedLoads) {
        // prompt driver to select which loads he wants to receive. (per compartment)
        // for each selection: prompt driver to select meter from
        // load.getRecipe().getAvailableMeterAssignments();
        
    }

    private void deliverManualLoad() {

    }

    private void cancelLoad() {
    }

    private void processLoadFulfillment() {
        // collect loaded details from meters.
        // record journal entry.
        // publishFulfilledLoadMessage(...);
    }

    private void decorateAvailableMeterAssignments(Load load) {
        getSpots().stream().forEach(spot -> {
            var groupedMeterAssignments = groupMeterAssignmentsByMeter(spot.getMeterAssignments());
            groupedMeterAssignments.stream().forEach(meterAssignments -> {
                if (load.getRecipe().getProducts().stream().allMatch(p -> meterAssignments.stream()
                        .anyMatch(assignment -> assignment.getProductCode().equals(p.getCode())))) {
                    if (load.getRecipe().getAdditives().stream().allMatch(a -> meterAssignments.stream()
                            .anyMatch(assignment -> assignment.getAdditiveCode().equals(a.getCode())))) {
                        load.getRecipe().getAvailableMeters().add(meterAssignments.stream().findFirst().get().getMeterId());
                    }
                }
            });
        });
    }

    private List<Spot> getSpots() {
        return new ArrayList<Spot>();
    }

    private List<List<MeterAssignment>> groupMeterAssignmentsByMeter(List<MeterAssignment> assignments) {
        List<List<MeterAssignment>> groupedLists = new ArrayList<List<MeterAssignment>>();
        List<MeterAssignment> meterAssignments = new ArrayList<MeterAssignment>();
        groupedLists.add(meterAssignments);
        return groupedLists;
    }

    private void assertAtleastOneCompatibleSpot(Load load) throws Exception {
        if (load.getRecipe().getAvailableMeters().isEmpty()) {
            throw new Exception("No compatible spots found for load.");
        }
    }

    private void assertDriverHasPhysicalAccess(Load load) throws Exception {
        if (false) {
            throw new Exception("Driver does not have physical access.");
        }
    }
}

@Getter
@Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
@ToString
class Spot {
    private List<MeterAssignment> meterAssignments;
}

@Getter
@Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
@ToString
class Meter {
    private String unid;
    private String meterId;
    private List<MeterAssignment> meterAssignments;
}

@Getter
@Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
@ToString
class MeterAssignment {
    private String unid;
    private String meterId;
    private String productCode;
    private String additiveCode;
}

@Getter
@Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
@ToString
class Load {
    private String unid;
    private String correlationId;
    private Destination destination;
    private Recipe recipe;
    private Carrier carrier;
    private Driver driver;
    private Trailer trailer;
}

@Getter
@Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
@ToString
class Recipe {
    private String name;
    private String code;
    private String description;

    private List<Product> products;
    private List<Additive> additives;
    private List<String> availableMeters;
    private Meter selectedMeter;

}

@Getter
@Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
@ToString
class Additive {
    private String name;
    private String code;
    private String description;

}

@Getter
@Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
@ToString
class Product {
    private String name;
    private String code;
    private String description;
    private Supplier supplier;
    private LoadingControl loadingControl;

}

@Getter
@Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
@ToString
class Carrier {

}

@Getter
@Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
@ToString
class Driver {

}

@Getter
@Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
@ToString
class Trailer {

}

@Getter
@Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
@ToString
class Supplier {

}

@Getter
@Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
@ToString
class LoadingControl {

}

@Getter
@Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
@ToString
class Destination {

}
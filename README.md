# di-cri-experian-api

## Running locally

### Cross-core API

Environment variables:
````
export EXPERIAN_CROSS_CORE_API_TENANT_ID=
export EXPERIAN_CROSS_CORE_API_ENDPOINT_URI=
export EXPERIAN_CROSS_CORE_API_HMAC_KEY=
export EXPERIAN_CROSS_CORE_API_KEYSTORE_PATH=
export EXPERIAN_CROSS_CORE_API_KEYSTORE_PASSWORD=
````

Execute: `cd experian-api && ../gradlew run` to build and run the project

Sample request:
````
POST /identity-check
{
    "firstName": "JON",
    "middleNames": null,
    "surname": "DOE",
    "dateOfBirth": "1970-01-01",
    "addresses" : [
        {
            "houseNameNumber": "70",
            "street" : "WHITEHALL",
            "townCity": "LONDON",
            "postcode": "SW1A 2AS",
            "addressType": "CURRENT"    
        }
    ] 
}
````

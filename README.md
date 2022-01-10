# di-cri-experian-api

## Running locally

### Cross-core API

Environment variables:
````
export EXPERIAN_API_TENANT_ID=
export EXPERIAN_API_ENDPOINT_URI=
export EXPERIAN_API_HMAC_KEY=
export KEYSTORE_PATH=
export KEYSTORE_PASSWORD=
````

Execute: `./gradlew run` to build and run the project

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

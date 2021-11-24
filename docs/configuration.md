# Configuration

## Project Structure

Redmatch projects have the following structure:

```
redmatch-project
├── schema.csv
├── file1.rdm
├── file2.rdm
├── file3.rdm
└── redmatch-config.yaml
```

At a minimum a project should have a schema file (`schema.csv` in this example), one or more Redmatch transformation rules documents (`file1..3.rd` in this example) and a configuration file (`redmatch-config.yml` in this example).

## REDCap Schema Files

Redmatch requires a local copy of the REDCap schema file to access the metadata of the fields. Both `csv` and `json` versions are supported. The local schema should match the schema of the remote server where the data will be retrieved.

### Redmatch Transformation Rules

Redmatch transformation rules are text files with an `.rdm` extension. The format is described in detail in the [reference page](./reference.md).

## REDCap Servers Configuration

The __redmatch-config.yaml__ file can be used to configure REDCap servers that can be referenced in the transformation rules. Once the transformation rules are defined, these servers can be used to access the REDCap API, retrieve data and transform it to FHIR. The following is an example that defines two REDCap servers:

```
servers:
- name: test
  type: redcap
  url: http://myserver.org/redcap/api/
  token: xxx
- name: local
  type: redcap
  url: http://localhost:8888/redcap/api/
  token: yyy
```

Each server needs the following:
 - a `name`, which can be used to reference the server in the transformation rules
 - a `type` which at the moment will always be `redcap` (Redmatch will support other sources in future releases so this property will indicate the type of server)
 - the `url` of the REDCap API endpoint
 - the `token` that is required to access the REDCap API

[Home](./index.html)



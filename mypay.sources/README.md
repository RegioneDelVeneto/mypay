# MyPay4

This is the repository of MyPay 4 suite.

MyPay module includes:

- functionalities for any citizen:
  - online payments;
  - prepare payments to be concluded offline;
  - retrieve info of payments made in the past;
- functionalities for the back-end officer:
  - manage "ente" (payments beneficiary);
  - manage "tipo dovuto";
  - retrieve info and manage payments of citizens.

## Project structure

- Root
  - `README.md` [this file]
  - `db` [database scripts]
  - `mypay4-batch` [batch related sources (both Talend and plain Java)]
  - `mypay4-be` [Java SpringBoot app that provides back-end REST services (both for citizens and back-end officers)]
  - `mypay4-fe` [root folder of front-end apps (both citizen app and officer app)]


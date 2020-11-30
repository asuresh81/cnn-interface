#!/usr/bin/env bash
set -e

absolute_path=`pwd`
parent_dir=${PWD##*/}
env_path="${absolute_path}/${parent_dir}_env"
expected_python_version="3.7"
reqs_file="${absolute_path}/requirements.txt"
user_python_version=`python3.7 --version`

[[ $user_python_version =~ [^[:space:]]*[0-3][.][0-7] ]]
if [[ ${BASH_REMATCH[0]} != $expected_python_version ]]; then
  echo "Python version ${BASH_REMATCH[0]} found."
  echo "Please ensure the latest version of Python $expected_python_version is installed."
  echo "Refer to our user guide for additional help."
else
  echo ""
  echo "Python version is correct"
  echo ""
  if [[ ! -d "$env_path" ]]; then

    set +e
    echo ""
    echo "Creating virtual environment in $parent_dir..."
    echo ""
    python3.7 -m venv $env_path

    if [[ $? -ne 0 ]]; then
      echo "There was an issue setting up the virtual environment. Please make sure Python 3.7 is installed properly."
      echo "You can verify this by running 'which python3.7' to make sure that Python 3.7 has been installed."
      exit 1
    fi

    echo ""
    echo "Virtual environment has been successfully created."
    echo "Activating virtual environment to install necessary packages"
    echo ""

    source "$env_path/bin/activate"

    if [[ $? -ne 0 ]]; then
      echo "There was an issue activating the virtual environment. Please make sure this setup script is located in the correct place."
      echo "It should be located in the directory that has all the needed files for your CNN model."
      echo "Please refer to the user guide for more information."
      exit 1
    fi

    echo ""
    echo "Successfully activated the virtual environment"
    echo ""

    if [[ -f "$reqs_file" ]]; then
      echo ""
      echo "Upgrading pip to latest version before installing required packages."
      echo ""
      pip install --upgrade pip
      if [[ $? -ne 0 ]]; then
        echo "There was an error while upgrading pip. Please make sure Python 3.7 has been installed correctly."
      fi
      echo ""
      echo "Installing required packages from requirements.txt..."
      echo ""
      set +e
      pip install -r requirements.txt
      if [[ $? -ne 0 ]]; then
        echo ""
        echo "There was an issue installing the required packages."
        echo "Please make sure the requirements.txt file is correct and try again."
        echo ""
        exit 1
      fi
    else
      echo "There is no requirements.txt file located in $parent_dir."
      echo "Please re-run this script after moving it to $parent_dir."
    fi

  else
    echo "The virtual environment has already been created."
    echo "We will verify that we can use it and then make sure that all necessary packages are installed."
    set +e
    source "$env_path/bin/activate"
    if [[ $? -ne 0 ]]; then
      echo ""
      echo "There was an issue activating the virtual environment. We will try creating a new one."
      echo "Creating virtual environment in $parent_dir..."
      python3.7 -m venv $env_path
      if [[ $? -ne 0 ]]; then
        echo ""
        echo "We were unable to create the virtual environment. Please delete the virtual "
        echo "environment folder in $parent_dir manually and try again."
        echo ""
        exit 1
      fi
      echo ""
      echo "We successfully created the virtual environment. We will continue with the rest of the setup process."
      echo ""
    fi

    echo ""
    echo "Activating the virtual environment..."
    echo ""
    source "$env_path/bin/activate"
    if [[ $? -ne 0 ]]; then
      echo "There was an issue activating the virtual environment. Since we have already tried creating another one, please manually delete "
      echo "the environment folder in $parent_dir and try running the setup script again."
      echo ""
      exit 1
    fi
    if [[ -f "$reqs_file" ]]; then
      echo ""
      echo "Upgrading pip to latest version before installing required packages."
      echo ""
      pip install --upgrade pip
      if [[ $? -ne 0 ]]; then
        echo "There was an issue upgrading pip. Please make sure Python 3.7 has been installed correctly."
        exit 1
      fi
      echo ""
      echo "Installing required packges from requirements.txt"
      echo ""
      pip install -r requirements.txt
      if [[ $? -ne 0 ]]; then
        echo ""
        echo "There was an issue installing the required packages."
        echo "Please make sure the requirements.txt file is correct and try again."
        echo ""
        exit 1
      fi

    else
      echo ""
      echo "There is no requirements.txt file located in $parent_dir."
      echo "Please re-run this script after moving it to $parent_dir."
      echo ""
      exit 1
    fi

  fi

fi

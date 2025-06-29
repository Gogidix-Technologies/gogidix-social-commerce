# Social Commerce Tools Archive

This directory contains utility scripts and tools for the Social Commerce domain.

## Contents

- `compile-services-simple.bat`: Batch script for compiling services
  - **Usage**: Run this script to compile all services in the domain
  - **Dependencies**: Java JDK, Maven

- `compile-social-commerce.ps1`: PowerShell script for compiling the social commerce services
  - **Usage**: Run this script with appropriate parameters
  - **Parameters**: 
    - `-Environment`: Specify the target environment (dev/stage/prod)
    - `-SkipTests`: Skip running tests during compilation

## Purpose

These tools are provided to assist with development and deployment tasks. Always review the scripts before execution and ensure you have the necessary permissions and dependencies installed.

## Security Note
- Never run scripts from untrusted sources
- Review scripts before execution
- Ensure proper permissions are set on script files

## Last Updated
June 3, 2025

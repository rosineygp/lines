#!/bin/bash

set -e

echo "patching flk"

_command_exist() {
  local -r cmd="${1}"
  if ! [[ -x $(command -v "${cmd}") ]]; then
    echo -e "\ncommand: $cmd not found, please install it.\n"
    exit 1
  fi
}

_code_block() {
  local filter_call="^(load-file-without-hashbang"
  local f="${1}"
  local d="${2}"

  HEREDOC="$(sed -E 's/(\/|\.|-)/_/g;s/[a-z]/\U&/g' <<< ${f})"

{
    echo -e "read -d \"\" __FLECK__REPCAPTURE_${HEREDOC} << __LINES_INLINE_${HEREDOC}";
    grep -v "$filter_call" "${f}" | sed -e 's/\\/\\\\\\\\/g;/^;/d' ;
    echo -e "\n__LINES_INLINE_${HEREDOC}";
    echo -e "REP \"(do \${__FLECK__REPCAPTURE_${HEREDOC}})\";";
  } >> "${d}"
}

_branch_or_tag_name() {
  local branch="unknown"
  if [[ "$GITHUB_ACTIONS" == "true" ]]; then
    branch=$(echo "${GITHUB_REF}" | cut -d'/' -f3-)
    echo -ne "${branch}"
  elif [[ "${GITLAB_CI}" == "true" ]]; then
    echo -ne "${CI_COMMIT_REF_NAME}"
  elif [[ "${JENKINS_URL}" != "" ]]; then
    echo -ne "${GIT_BRANCH}"
  elif [[ "${TRAVIS}" == "true" ]]; then
    echo -ne "${TRAVIS_BRANCH}"
  elif [[ "${CIRCLECI}" == "true" ]]; then
    if [[ "${CIRCLE_TAG}" != "" ]]; then
      echo -ne "${CIRCLE_TAG}"
    else
      echo -ne "${CIRCLE_BRANCH}"
    fi
  elif [[ $(git rev-parse --is-inside-work-tree) == true ]]; then
    branch=$(git rev-parse --abbrev-ref HEAD)
    echo -ne "${branch}"
  else
    echo -ne "${branch}"
  fi
}

_command_exist "patch"
_command_exist "curl"
_command_exist "sed"

flk_url="https://raw.githubusercontent.com/chr15m/flk/beabb477daac370f5fa52eabdec7dc977b944198/docs/flk"

set -x
curl "${flk_url}" > .flk

for i in patches/*.flk.patch; do
  patch -i "$i" -u .flk
done

chmod +x .flk
cp .flk flk

patch -i "patches/027-boot.lines.patch" -u .flk

_code_block "src/includes/lang-utils.clj" ".flk"
_code_block "src/args.clj" ".flk"
_code_block "src/includes/use.clj" ".flk"
_code_block "src/includes/colors.clj" ".flk"
_code_block "src/includes/pretty-print.clj" ".flk"
_code_block "src/core.clj" ".flk"
_code_block "src/modules/docker.clj" ".flk"
_code_block "src/modules/shell.clj" ".flk"
_code_block "src/modules/template.clj" ".flk"
_code_block "src/modules/scp.clj" ".flk"
_code_block "src/main.clj" ".flk"

sed -i --regexp-extended '/^([ ]+#|#)/d;/^$/d;s/[ \t]*$//;s/^[ \t]*//;/^$/d' .flk
sed -i '1 s/^/#\!\/usr\/bin\/env bash\n/' .flk

#set version
_version=$(_branch_or_tag_name)
sed -i "s/__LINES_REPLACE_VERSION/$_version/g" .flk

# footer injection (spawn errors)
echo -n '[ "${r}" = "nil" ] && exit 0 || { echo "${r}"; exit 127; };' >> .flk

mv .flk lines
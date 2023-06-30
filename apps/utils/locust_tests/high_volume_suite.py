"""High Volume Load test suite for BFD Server endpoints."""
import sys, inspect
from random import shuffle
from typing import Callable, List, TypeVar, Optional, Type, Dict, Set, Protocol

from locust import TaskSet, events, tag, task
from locust.env import Environment

from common import data, db
from common.bfd_user_base import BFDUserBase
from common.locust_utils import is_distributed, is_locust_master
from common.url_path import create_url_path
from common.user_init_aware_load_shape import UserInitAwareLoadShape

TaskT = TypeVar("TaskT", Callable[..., None], Type["TaskSet"])
MASTER_BENE_IDS: List[str] = []
MASTER_CONTRACT_DATA: List[Dict[str, str]] = []
MASTER_HASHED_MBIS: List[str] = []
TAGS: Set[str] = []
EXCLUDE_TAGS: Set[str] = []

@events.test_start.add_listener
def _(environment: Environment, **kwargs):
    if (
        is_distributed(environment)
        and is_locust_master(environment)
        or not environment.parsed_options
    ):
        return

    # See https://docs.locust.io/en/stable/extending-locust.html#test-data-management
    # for Locust's documentation on the test data management pattern used here
    global MASTER_BENE_IDS
    MASTER_BENE_IDS = data.load_from_parsed_opts(
        environment.parsed_options,
        db.get_bene_ids,
        use_table_sample=True,
        data_type_name="bene_ids",
    )

    global TAGS
    TAGS = environment.parsed_options.locust_tags.split() if hasattr(environment.parsed_options, "locust_tags") else []

    global EXCLUDE_TAGS
    EXCLUDE_TAGS = environment.parsed_options.locust_exclude_tags.split() if hasattr(environment.parsed_options, "locust_exclude_tags") else []

    global MASTER_CONTRACT_DATA
    MASTER_CONTRACT_DATA = data.load_from_parsed_opts(
        environment.parsed_options,
        db.get_contract_ids,
        use_table_sample=True,
        data_type_name="contract_data",
    )

    global MASTER_HASHED_MBIS
    MASTER_HASHED_MBIS = data.load_from_parsed_opts(
        environment.parsed_options,
        db.get_hashed_mbis,
        use_table_sample=True,
        data_type_name="hashed_mbis",
    )

class TestLoadShape(UserInitAwareLoadShape):
    pass

class TaskHolder(Protocol[TaskT]):
    tasks: List[TaskT]

EOB_TAG = "eob"
@tag(EOB_TAG)
@task
class EobTaskSet(TaskSet):
    @tag("eob_test_id_count_type_pde_v1", "v1")
    @task
    def eob_test_id_count_type_pde_v1(self):
        """Explanation of Benefit search by ID, type PDE, paginated"""
        self.run_task_by_parameters(
            base_path="/v1/fhir/ExplanationOfBenefit",
            params={
                "patient":  self.bene_ids.pop(),
                "_format": "json",
                "_count": "50",
                "_types": "PDE",
            },
            name="/v1/fhir/ExplanationOfBenefit search by id / type = PDE / count = 50",
        )

    @tag("eob_test_id_last_updated_count_v1", "v1")
    @task
    def eob_test_id_last_updated_count_v1(self):
        """Explanation of Benefit search by ID, last updated, paginated"""
        self.run_task_by_parameters(
            base_path="/v1/fhir/ExplanationOfBenefit",
            params={
                "patient":  self.bene_ids.pop(),
                "_format": "json",
                "_count": "100",
                "_lastUpdated": f"gt{ self.last_updated}",
            },
            name="/v1/fhir/ExplanationOfBenefit search by id / lastUpdated / count = 100",
        )

    @tag("eob_test_id_include_tax_number_last_updated_v1", "v1")
    @task
    def eob_test_id_include_tax_number_last_updated_v1(self):
        """Explanation of Benefit search by ID, Last Updated, Include Tax Numbers"""
        self.run_task_by_parameters(
            base_path="/v1/fhir/ExplanationOfBenefit",
            params={
                "patient":  self.bene_ids.pop(),
                "_format": "json",
                "_lastUpdated": f"gt{ self.last_updated}",
                "_IncludeTaxNumbers": "true",
            },
            name="/v1/fhir/ExplanationOfBenefit search by id / lastUpdated / includeTaxNumbers",
        )

    @tag("eob_test_id_last_updated_v1", "v1")
    @task
    def eob_test_id_last_updated_v1(self):
        """Explanation of Benefit search by ID, Last Updated"""
        self.run_task_by_parameters(
            base_path="/v1/fhir/ExplanationOfBenefit",
            params={
                "patient":  self.bene_ids.pop(),
                "_format": "json",
                "_lastUpdated": f"gt{ self.last_updated}",
            },
            name="/v1/fhir/ExplanationOfBenefit search by id / lastUpdated",
        )

    @tag("eob_test_id_v1", "v1")
    @task
    def eob_test_id_v1(self):
        """Explanation of Benefit search by ID"""
        self.run_task_by_parameters(
            base_path="/v1/fhir/ExplanationOfBenefit",
            params={"patient":  self.bene_ids.pop(), "_format": "application/fhir+json"},
            name="/v1/fhir/ExplanationOfBenefit search by id",
        )

    @tag("eob_test_id", "v2")
    @task
    def eob_test_id(self):
        """Explanation of Benefit search by ID"""
        self.run_task_by_parameters(
            base_path="/v2/fhir/ExplanationOfBenefit",
            params={"patient":  self.bene_ids.pop(), "_format": "application/fhir+json"},
            name="/v2/fhir/ExplanationOfBenefit search by id",
        )

    @tag("eob_test_id_count", "v2")
    @task
    def eob_test_id_count(self):
        """Explanation of Benefit search by ID, Paginated"""
        self.run_task_by_parameters(
            base_path="/v2/fhir/ExplanationOfBenefit",
            params={
                "patient":  self.bene_ids.pop(),
                "_count": "10",
                "_format": "application/fhir+json",
            },
            name="/v2/fhir/ExplanationOfBenefit search by id / count=10",
        )

    @tag("eob_test_id_include_tax_number_last_updated", "v2")
    @task
    def eob_test_id_include_tax_number_last_updated(self):
        """Explanation of Benefit search by ID, Last Updated, Include Tax Numbers"""
        self.run_task_by_parameters(
            base_path="/v2/fhir/ExplanationOfBenefit",
            params={
                "_lastUpdated": f"gt{ self.last_updated}",
                "patient":  self.bene_ids.pop(),
                "_IncludeTaxNumbers": "true",
                "_format": "application/fhir+json",
            },
            name="/v2/fhir/ExplanationOfBenefit search by id / lastUpdated / includeTaxNumbers",
        )

COVERAGE_TAG = "coverage"
@tag(COVERAGE_TAG)
@task
class CoverageTaskSet(TaskSet):
    @tag("coverage_test_id_count_v1", "v1")
    @task
    def coverage_test_id_count_v1(self):
        """Coverage search by ID, Paginated"""
        self.run_task_by_parameters(
            base_path="/v1/fhir/Coverage",
            params={"beneficiary":  self.bene_ids.pop(), "_count": "10"},
            name="/v1/fhir/Coverage search by id / count=10",
        )

    @tag("coverage_test_id_last_updated_v1", "v1")
    @task
    def coverage_test_id_last_updated_v1(self):
        """Coverage search by ID, Last Updated"""
        self.run_task_by_parameters(
            base_path="/v1/fhir/Coverage",
            params={
                "_lastUpdated": f"gt{ self.last_updated}",
                "beneficiary":  self.bene_ids.pop(),
            },
            name="/v1/fhir/Coverage search by id / lastUpdated (2 weeks)",
        )

    @tag("coverage_test_id", "v2")
    @task
    def coverage_test_id(self):
        """Coverage search by ID"""
        self.run_task_by_parameters(
            base_path="/v2/fhir/Coverage",
            params={
                "beneficiary":  self.bene_ids.pop(),
            },
            name="/v2/fhir/Coverage search by id",
        )

    @tag("coverage_test_id_count", "v2")
    @task
    def coverage_test_id_count(self):
        """Coverage search by ID, Paginated"""
        self.run_task_by_parameters(
            base_path="/v2/fhir/Coverage",
            params={"beneficiary":  self.bene_ids.pop(), "_count": "10"},
            name="/v2/fhir/Coverage search by id / count=10",
        )

    @tag("coverage_test_id_last_updated", "v2")
    @task
    def coverage_test_id_last_updated(self):
        """Coverage search by ID, Last Updated"""
        self.run_task_by_parameters(
            base_path="/v2/fhir/Coverage",
            params={
                "_lastUpdated": f"gt{self.last_updated}",
                "beneficiary":  self.bene_ids.pop(),
            },
            name="/v2/fhir/Coverage search by id / lastUpdated (2 weeks)",
        )

PATIENT_TAG = "patient"
@tag(PATIENT_TAG)
@task
class PatientTaskSet(TaskSet):
    @tag("patient_test_coverage_contract_v1", "v1")
    @task
    def patient_test_coverage_contract_v1(self):
        """Patient search by coverage contract (all pages)"""

        def make_url():
            contract = self.contract_data.pop()
            return create_url_path(
                "/v1/fhir/Patient",
                {
                    "_has:Coverage.extension": f'https://bluebutton.cms.gov/resources/variables/ptdcntrct01|{contract["id"]}',
                    "_has:Coverage.rfrncyr": f'https://bluebutton.cms.gov/resources/variables/rfrnc_yr|{contract["year"]}',
                    "_count": 25,
                    "_format": "json",
                },
            )

        self.run_task(
            name="/v1/fhir/Patient search by coverage contract (all pages)",
            headers={"IncludeIdentifiers": "mbi"},
            url_callback=make_url,
        )

    @tag("patient_test_hashed_mbi_v1", "v1")
    @task
    def patient_test_hashed_mbi_v1(self):
        """Patient search by ID, Last Updated, include MBI, include Address"""

        def make_url():
            return create_url_path(
                "/v1/fhir/Patient/",
                {
                    "identifier": f"https://bluebutton.cms.gov/resources/identifier/mbi-hash|{ self.hashed_mbis.pop()}",
                    "_IncludeIdentifiers": "mbi",
                },
            )

        self.run_task(
            name="/v1/fhir/Patient search by hashed mbi / includeIdentifiers = mbi",
            url_callback=make_url,
        )

    @tag("patient_test_id_last_updated_include_mbi_include_address_v1", "v1")
    @task
    def patient_test_id_last_updated_include_mbi_include_address_v1(self):
        """Patient search by ID, Last Updated, include MBI, include Address"""
        self.run_task_by_parameters(
            base_path="/v1/fhir/Patient",
            params={
                "_id":  self.bene_ids.pop(),
                "_lastUpdated": f"gt{ self.last_updated}",
                "_IncludeIdentifiers": "mbi",
                "_IncludeTaxNumbers": "true",
            },
            name="/v1/fhir/Patient/id search by id / (2 weeks) / includeTaxNumbers / mbi",
        )

    @tag("patient_test_id_v1", "v1")
    @task
    def patient_test_id_v1(self):
        """Patient search by ID"""

        def make_url():
            return create_url_path(f"/v1/fhir/Patient/{ self.bene_ids.pop()}", {})

        self.run_task(name="/v1/fhir/Patient/id", url_callback=make_url)

    @tag("patient_test_coverage_contract", "v2")
    @task
    def patient_test_coverage_contract(self):
        """Patient search by Coverage Contract, paginated"""
        def make_url():
            contract =  self.contract_data.pop()
            return create_url_path(
                "/v2/fhir/Patient",
                {
                    "_has:Coverage.extension": f'https://bluebutton.cms.gov/resources/variables/ptdcntrct01|{contract["id"]}',
                    "_has:Coverage.rfrncyr": f'https://bluebutton.cms.gov/resources/variables/rfrnc_yr|{contract["year"]}',
                    "_count": 25,
                    "_format": "json",
                },
            )

        self.run_task(
            name="/v2/fhir/Patient search by coverage contract (all pages)",
            headers={"IncludeIdentifiers": "mbi"},
            url_callback=make_url,
        )

    @tag("patient_test_hashed_mbi", "v2")
    @task
    def patient_test_hashed_mbi(self):
        """Patient search by hashed MBI, include identifiers"""
        def make_url():
            return create_url_path(
                "/v2/fhir/Patient/",
                {
                    "identifier": f"https://bluebutton.cms.gov/resources/identifier/mbi-hash|{ self.hashed_mbis.pop()}",
                    "_IncludeIdentifiers": "mbi",
                },
            )

        self.run_task(
            name="/v2/fhir/Patient search by hashed mbi / includeIdentifiers = mbi",
            url_callback=make_url,
        )

    @tag("patient_test_id_include_mbi_last_updated", "v2")
    @task
    def patient_test_id_include_mbi_last_updated(self):
        """Patient search by ID with last updated, include MBI"""
        self.run_task_by_parameters(
            base_path="/v2/fhir/Patient",
            params={
                "_id":  self.bene_ids.pop(),
                "_format": "application/fhir+json",
                "_IncludeIdentifiers": "mbi",
                "_lastUpdated": f"gt{ self.last_updated}",
            },
            name="/v2/fhir/Patient search by id / _IncludeIdentifiers=mbi / (2 weeks)",
        )

    @tag("patient_test_id", "v2")
    @task
    def patient_test_id(self):
        """Patient search by ID"""
        self.run_task_by_parameters(
            base_path="/v2/fhir/Patient",
            params={
                "_id":  self.bene_ids.pop(),
                "_format": "application/fhir+json",
            },
            name="/v2/fhir/Patient search by id",
        )

class HighVolumeUser(BFDUserBase):
    """High volume load test suite for V2 BFD Server endpoints.

    The tests in this suite generate a large volume of traffic to endpoints that are hit most
    frequently during a peak load event.
    """

    # Do we terminate the tests when a test runs out of data and paginated URLs?
    END_ON_NO_DATA = False

    @staticmethod
    def filter_tasks_by_tags(
        task_holder: Type[TaskHolder],
        tags: Set[str],
        exclude_tags: Set[str],
        checked: Optional[Dict[TaskT, bool]] = None,
    ):
        """
        Recursively remove any tasks/TaskSets from a TaskSet/User that
        shouldn't be executed according to the tag options
        :param task_holder: the TaskSet or User with tasks
        :param tags: The set of tasks by @tag to include in the final list
        :param exclude_tags: The set of tasks by @tag to exclude from the final list
        :param checked: The running score of tasks which have or have not been processed
        :return: A list of filtered tasks to execute
        """
        filtered_tasks = []
        if checked is None:
            checked = {}
        for task in task_holder.tasks:
            if task in checked:
                if checked[task]:
                    filtered_tasks.append(task)
                continue
            passing = True
            if hasattr(task, "tasks"):
                self.filter_tasks_by_tags(task, tags, exclude_tags, checked)
                passing = len(task.tasks) > 0
            else:
                if len(tags) > 0:
                    passing &= "locust_tag_set" in dir(task) and len(task.locust_tag_set.intersection(tags)) > 0
                if len(exclude_tags) > 0:
                    passing &= "locust_tag_set" not in dir(task) or len(task.locust_tag_set.intersection(exclude_tags)) == 0

            if passing:
                filtered_tasks.append(task)
            checked[task] = passing

        return filtered_tasks

    @staticmethod
    def get_tasks(tags: Set[str], exclude_tags: Set[str]):
        """
        Returns the list of runnable tasks for the given user, filterable by a list of tags or exclude_tags.
        Returns all runnable tasks if neither tags or exclude_tags contain items.

        :param tags: The list of tags to filter tasks by
        :param exclude_tags: This list of tags to exclude tasks by
        :return: A list of tasks to run
        """

        # Filter out the class members without a tasks attribute
        class_members = inspect.getmembers(sys.modules[__name__], inspect.isclass)
        potential_tasks = list(filter(lambda potential_task: hasattr(potential_task[1], "tasks"), class_members))

        # Filter each task holder's tasks by the given tags and exclude_tags
        tasks = []
        for task_holder in list(map(lambda task_set: task_set[1], potential_tasks)):
            tasks.extend(HighVolumeUser.filter_tasks_by_tags(task_holder, tags, exclude_tags))
        return tasks

    def get_runnable_tasks(self, tags: Set[str], exclude_tags: Set[str]):
        """
        Helper method to be called via the HighVolumerUser constructor.
        Required due to python <= 3.9 not allowing direct calls to static methods.
        Returns the list of runnable tasks.

        :param tags: The list of tags to filter tasks by
        :param exclude_tags: This list of tags to exclude tasks by
        :return: A list of tasks to run
        """
        return HighVolumeUser.get_tasks(tags, exclude_tags)

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.bene_ids = MASTER_BENE_IDS.copy()
        self.contract_data = MASTER_CONTRACT_DATA.copy()
        self.hashed_mbis = MASTER_HASHED_MBIS.copy()

        # As of 01/20/2023 there is an unresolved locust issue [1] with the --tags/--exclude-tags command line options.
        # Therefore, we have implemented custom arguments (--locust-tags/--locust-exclude-tags) to programmatically
        # filter tasks by the given @tag(s) at runtime.
        # [1] https://github.com/locustio/locust/issues/1689
        self.tasks = self.get_runnable_tasks(TAGS, EXCLUDE_TAGS)

        # Shuffle all the data around so that each HighVolumeUser is _probably_
        # not requesting the same data.
        shuffle(self.bene_ids)
        shuffle(self.contract_data)
        shuffle(self.hashed_mbis)

        # Override the value for last_updated with a static value
        self.last_updated = "2022-06-29"
